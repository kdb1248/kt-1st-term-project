package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.MenuDto;
import com.kt.hiorder_backend.dto.RestaurantMenuInfoResponse;
import com.kt.hiorder_backend.entity.Menu;
import com.kt.hiorder_backend.entity.MenuCategory;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.MenuCategoryRepository;
import com.kt.hiorder_backend.repository.MenuRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantMenuInfoService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;

    public RestaurantMenuInfoService(RestaurantRepository restaurantRepository,
                                     MenuCategoryRepository menuCategoryRepository,
                                     MenuRepository menuRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.menuRepository = menuRepository;
    }

    public RestaurantMenuInfoResponse getMenuList(Long restaurantId, Long menuCategoryId, String sortParam, String lang) {
        // 1) 식당 존재 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));

        // 2) 카테고리 존재 + 식당 소속 여부 확인
        MenuCategory category = menuCategoryRepository.findById(menuCategoryId)
                .orElseThrow(() -> new RuntimeException("해당 카테고리를 찾을 수 없습니다."));

        if (!category.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("해당 카테고리가 이 식당에 속해있지 않습니다.");
        }

        // 3) 메뉴 목록 조회
        List<Menu> menus;
        if ("displayOrder".equals(sortParam)) {
            menus = menuRepository.findByRestaurant_RestaurantIdAndMenuCategory_MenuCategoryIdOrderByDisplayOrderAsc(restaurantId, menuCategoryId);
        } else {
            // 기본 정렬 (예: menu_id asc) or 다른 로직
            menus = menuRepository.findByRestaurant_RestaurantIdAndMenuCategory_MenuCategoryId(restaurantId, menuCategoryId);
        }

        // 4) DTO 변환
        List<MenuDto> menuDtoList = menus.stream()
                .map(m -> MenuDto.builder()
                        .menuId(m.getMenuId())
                        .menuName(getMenuNameByLang(m, lang)) // [CHANGED]
                        .price(m.getPrice())
                        .menuDescription(getMenuDescByLang(m, lang)) // [CHANGED]
                        .menuImageUrl(m.getMenuImageUrl())
                        .displayOrder(m.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        // 5) 응답 구성
        return RestaurantMenuInfoResponse.builder()
                .status(200)
                .success(true)
                .data(menuDtoList)
                .message("메뉴 조회에 성공했습니다.")
                .build();
    }
    // [CHANGED] lang에 따라 메뉴명 반환
    private String getMenuNameByLang(Menu m, String lang) {
        switch (lang.toLowerCase()) {
            case "en":
                return (m.getMenuNameEn() != null && !m.getMenuNameEn().isEmpty())
                        ? m.getMenuNameEn()
                        : m.getMenuName();
            case "zh":
                return (m.getMenuNameZh() != null && !m.getMenuNameZh().isEmpty())
                        ? m.getMenuNameZh()
                        : m.getMenuName();
            case "jp":
                return (m.getMenuNameJp() != null && !m.getMenuNameJp().isEmpty())
                        ? m.getMenuNameJp()
                        : m.getMenuName();
            default:
                // kr or 기타
                return m.getMenuName();
        }
    }

    // [CHANGED] lang에 따라 메뉴설명 반환
    private String getMenuDescByLang(Menu m, String lang) {
        switch (lang.toLowerCase()) {
            case "en":
                return (m.getMenuDescriptionEn() != null && !m.getMenuDescriptionEn().isEmpty())
                        ? m.getMenuDescriptionEn()
                        : m.getMenuDescription();
            case "zh":
                return (m.getMenuDescriptionZh() != null && !m.getMenuDescriptionZh().isEmpty())
                        ? m.getMenuDescriptionZh()
                        : m.getMenuDescription();
            case "jp":
                return (m.getMenuDescriptionJp() != null && !m.getMenuDescriptionJp().isEmpty())
                        ? m.getMenuDescriptionJp()
                        : m.getMenuDescription();
            default:
                return m.getMenuDescription();
        }
    }
}

