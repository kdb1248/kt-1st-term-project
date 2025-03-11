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

    public RestaurantMenuInfoResponse getMenuList(Long restaurantId, Long menuCategoryId, String sortParam) {
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
                        .menuName(m.getMenuName())
                        .price(m.getPrice())
                        .menuDescription(m.getMenuDescription())
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
}

