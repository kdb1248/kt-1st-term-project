package com.kt.hiorder_backend.service;

import org.springframework.stereotype.Service;

import com.kt.hiorder_backend.dto.MenuCreateRequest;
import com.kt.hiorder_backend.dto.MenuCreateResponse;
import com.kt.hiorder_backend.entity.Menu;
import com.kt.hiorder_backend.entity.MenuCategory;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.MenuCategoryRepository;
import com.kt.hiorder_backend.repository.MenuRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;

import jakarta.transaction.Transactional;

@Service
public class MenuCreateService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final TranslationService translationService;

    public MenuCreateService(
            MenuRepository menuRepository,
            MenuCategoryRepository menuCategoryRepository,
            RestaurantRepository restaurantRepository,
            TranslationService translationService
    ) {
        this.menuRepository = menuRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.translationService = translationService;
    }

    @Transactional
    public MenuCreateResponse createMenu(Long restaurantId, MenuCreateRequest request) {
        // 1) restaurantId 유효성 검증 (생략 가능)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException(
                "식당 정보를 찾을 수 없습니다. restaurantId=" + restaurantId
            ));

        // 2) 카테고리 처리
        MenuCategory category;
        if (request.getMenuCategoryId() != null) {
            category = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new RuntimeException(
                    "카테고리를 찾을 수 없습니다. menuCategoryId=" + request.getMenuCategoryId()
                ));

            // 카테고리 한글명이 새로 넘어왔으면 업데이트
            if (request.getMenuCategoryName() != null && !request.getMenuCategoryName().isEmpty()) {
                category.setMenuCategoryName(request.getMenuCategoryName());

                // 이미 번역된 데이터가 없는 경우에만 번역해서 채워넣기
                if (category.getMenuCategoryNameEn() == null
                    || category.getMenuCategoryNameEn().isEmpty()) {
                    String catNameEn = translationService.translateToEnglish(request.getMenuCategoryName());
                    category.setMenuCategoryNameEn(catNameEn);
                }
                if (category.getMenuCategoryNameZh() == null
                    || category.getMenuCategoryNameZh().isEmpty()) {
                    String catNameZh = translationService.translateToChinese(request.getMenuCategoryName());
                    category.setMenuCategoryNameZh(catNameZh);
                }
                if (category.getMenuCategoryNameJp() == null
                    || category.getMenuCategoryNameJp().isEmpty()) {
                    String catNameJp = translationService.translateToJapanese(request.getMenuCategoryName());
                    category.setMenuCategoryNameJp(catNameJp);
                }

                menuCategoryRepository.save(category);
            }
        } else {
            // 새 카테고리 등록
            if (request.getMenuCategoryName() == null || request.getMenuCategoryName().isEmpty()) {
                throw new IllegalArgumentException("카테고리 이름이 필요합니다.");
            }
            MenuCategory newCat = new MenuCategory();
            newCat.setRestaurant(restaurant);
            newCat.setMenuCategoryName(request.getMenuCategoryName());

            // 번역
            String catNameEn = translationService.translateToEnglish(request.getMenuCategoryName());
            String catNameZh = translationService.translateToChinese(request.getMenuCategoryName());
            String catNameJp = translationService.translateToJapanese(request.getMenuCategoryName());

            newCat.setMenuCategoryNameEn(catNameEn);
            newCat.setMenuCategoryNameZh(catNameZh);
            newCat.setMenuCategoryNameJp(catNameJp);

            category = menuCategoryRepository.save(newCat);
        }

        // 3) 메뉴 번역 (항상 새로 번역)
        String nameEn = translationService.translateToEnglish(request.getMenuName());
        String nameZh = translationService.translateToChinese(request.getMenuName());
        String nameJp = translationService.translateToJapanese(request.getMenuName());

        String descEn = translationService.translateToEnglish(request.getMenuDescription());
        String descZh = translationService.translateToChinese(request.getMenuDescription());
        String descJp = translationService.translateToJapanese(request.getMenuDescription());

        // 4) Menu 엔티티 생성
        Menu menu = new Menu();
        menu.setRestaurant(restaurant);
        menu.setMenuCategory(category);

        menu.setMenuName(request.getMenuName());
        menu.setMenuDescription(request.getMenuDescription());
        menu.setMenuNameEn(nameEn);
        menu.setMenuDescriptionEn(descEn);
        menu.setMenuNameZh(nameZh);
        menu.setMenuDescriptionZh(descZh);
        menu.setMenuNameJp(nameJp);
        menu.setMenuDescriptionJp(descJp);
        menu.setPrice(request.getPrice());
        menu.setMenuImageUrl(request.getMenuImageUrl());

        // [CHANGED] 5) displayOrder = (카테고리 내) 최댓값 + 1
        Integer maxOrder = menuRepository.findMaxDisplayOrderByCategoryId(category.getMenuCategoryId());
        if (maxOrder == null) {
            maxOrder = 0; 
        }
        menu.setDisplayOrder(maxOrder + 1);

        // 6) save
        Menu saved = menuRepository.save(menu);

        // 7) 응답
        return MenuCreateResponse.builder()
            .status(201)
            .success(true)
            .message("메뉴가 등록되었습니다.")
            .data(MenuCreateResponse.Data.builder()
                .menuId(saved.getMenuId())
                .menuCategoryId(category.getMenuCategoryId())
                .build()
            )
            .build();
    }
}
