package com.kt.hiorder_backend.service;

import org.springframework.stereotype.Service;

import com.kt.hiorder_backend.dto.MenuUpdateRequest;
import com.kt.hiorder_backend.dto.MenuUpdateResponse;
import com.kt.hiorder_backend.entity.Menu;
import com.kt.hiorder_backend.entity.MenuCategory;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.MenuCategoryRepository;
import com.kt.hiorder_backend.repository.MenuRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;

import jakarta.transaction.Transactional;

@Service
public class MenuUpdateService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final TranslationService translationService;

    public MenuUpdateService(
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
    public MenuUpdateResponse updateMenu(Long restaurantId, Long menuId, MenuUpdateRequest request) {
        // 1) 식당 존재 여부
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException(
                "식당 정보를 찾을 수 없습니다. restaurantId=" + restaurantId
            ));

        // 2) 메뉴 존재 여부
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new RuntimeException(
                "해당 메뉴를 찾을 수 없습니다. menuId=" + menuId
            ));

        // [CHANGED] 메뉴가 정말 이 식당 소속인지 확인 (선택)
        if (!menu.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("해당 메뉴는 다른 식당 소속입니다.");
        }

        // 3) 카테고리 수정 (optional)
        if (request.getMenuCategoryId() != null) {
            // (A) 기존 카테고리 찾기
            MenuCategory category = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new RuntimeException(
                    "카테고리를 찾을 수 없습니다. menuCategoryId=" + request.getMenuCategoryId()
                ));

            // (B) 한글 카테고리명 넘어오면 업데이트
            if (request.getMenuCategoryName() != null && !request.getMenuCategoryName().isEmpty()) {
                category.setMenuCategoryName(request.getMenuCategoryName());

                // 번역이 기존에 없으면 새로 번역
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

                // (C) 메뉴 엔티티에 새 카테고리 연결
                menu.setMenuCategory(category);
            }
            else {
                // 카테고리 id만 변경(한글명 안 넘어옴)
                menu.setMenuCategory(category);
            }
        }
        // else: 카테고리id 자체가 null이면 카테고리 변경 안 함

        // 4) 메뉴 수정
        // (A) 한글 메뉴명
        if (request.getMenuName() != null && !request.getMenuName().isEmpty()) {
            // 새 한글명 반영
            menu.setMenuName(request.getMenuName());

            // 번역 (항상 새로 번역한다고 가정)
            String nameEn = translationService.translateToEnglish(request.getMenuName());
            String nameZh = translationService.translateToChinese(request.getMenuName());
            String nameJp = translationService.translateToJapanese(request.getMenuName());

            menu.setMenuNameEn(nameEn);
            menu.setMenuNameZh(nameZh);
            menu.setMenuNameJp(nameJp);
        }

        // (B) 한글 메뉴 설명
        if (request.getMenuDescription() != null) {
            menu.setMenuDescription(request.getMenuDescription());

            // 번역 (항상 새로 번역한다고 가정)
            String descEn = translationService.translateToEnglish(request.getMenuDescription());
            String descZh = translationService.translateToChinese(request.getMenuDescription());
            String descJp = translationService.translateToJapanese(request.getMenuDescription());

            menu.setMenuDescriptionEn(descEn);
            menu.setMenuDescriptionZh(descZh);
            menu.setMenuDescriptionJp(descJp);
        }

        // (C) 가격
        if (request.getPrice() != null) {
            menu.setPrice(request.getPrice());
        }

        // (D) 이미지
        if (request.getMenuImageUrl() != null) {
            menu.setMenuImageUrl(request.getMenuImageUrl());
        }

        // (E) displayOrder는 수정하지 않는다고 가정(필요 시 처리)

        // 5) save
        Menu updated = menuRepository.save(menu);

        // 6) 응답
        return MenuUpdateResponse.builder()
            .status(200)
            .success(true)
            .message("메뉴가 수정되었습니다.")
            .data(MenuUpdateResponse.Data.builder()
                .menuId(updated.getMenuId())
                .menuCategoryId(updated.getMenuCategory().getMenuCategoryId())
                .menuName(updated.getMenuName())
                .menuDescription(updated.getMenuDescription())
                .menuImageUrl(updated.getMenuImageUrl())
                .price(updated.getPrice() != null ? updated.getPrice().longValue() : null)
                .build()
            )
            .build();
    }
}
