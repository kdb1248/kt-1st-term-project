package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.MenuCategoryDto;
import com.kt.hiorder_backend.dto.RestaurantCategoryInfoResponse;
import com.kt.hiorder_backend.entity.MenuCategory;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.MenuCategoryRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantCategoryInfoService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public RestaurantCategoryInfoService(RestaurantRepository restaurantRepository,
                                         MenuCategoryRepository menuCategoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuCategoryRepository = menuCategoryRepository;
    }

    public RestaurantCategoryInfoResponse getMenuCategories(Long restaurantId, String sortParam, String lang) {
        // 1) 식당 존재 여부 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));

        // 2) 카테고리 목록 조회
        // 예: sortParam이 "displayOrder"라면 displayOrder 기준 정렬
        //     sortParam이 null 또는 다른 값이라면 그냥 기본 정렬(예: id 순)
        List<MenuCategory> categories;
        if ("displayOrder".equals(sortParam)) {
            categories = menuCategoryRepository.findByRestaurant_RestaurantIdOrderByDisplayOrderAsc(restaurantId);
        } else {
            // 기본 정렬 (예: menuCategoryId asc) or 커스텀 로직
            categories = menuCategoryRepository.findByRestaurant_RestaurantId(restaurantId);
        }

        // 3) DTO 변환
        List<MenuCategoryDto> dtoList = categories.stream()
                .map(cat -> MenuCategoryDto.builder()
                        .menuCategoryId(cat.getMenuCategoryId())
                        .menuCategoryName(getCategoryNameByLang(cat, lang)) // [CHANGED]
                        .displayOrder(cat.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        // 4) 응답 구성
        return RestaurantCategoryInfoResponse.builder()
                .status(200)
                .success(true)
                .data(dtoList)
                .message("메뉴 카테고리 조회에 성공했습니다.")
                .build();
    }
    // [CHANGED] lang에 따라 카테고리명 반환
    private String getCategoryNameByLang(MenuCategory cat, String lang) {
        switch (lang.toLowerCase()) {
            case "en":
                return (cat.getMenuCategoryNameEn() != null && !cat.getMenuCategoryNameEn().isEmpty())
                        ? cat.getMenuCategoryNameEn()
                        : cat.getMenuCategoryName(); // fallback = 한글
            case "zh":
                return (cat.getMenuCategoryNameZh() != null && !cat.getMenuCategoryNameZh().isEmpty())
                        ? cat.getMenuCategoryNameZh()
                        : cat.getMenuCategoryName();
            case "jp":
                return (cat.getMenuCategoryNameJp() != null && !cat.getMenuCategoryNameJp().isEmpty())
                        ? cat.getMenuCategoryNameJp()
                        : cat.getMenuCategoryName();
            default:
                // "kr" or 기타
                return cat.getMenuCategoryName();
        }
    }
}

