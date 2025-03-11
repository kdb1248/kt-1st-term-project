package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.MenuDeleteResponse;
import com.kt.hiorder_backend.entity.Menu;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.MenuRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class MenuDeleteService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuDeleteService(MenuRepository menuRepository,
                             RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public MenuDeleteResponse deleteMenu(Long restaurantId, Long menuId) {
        // 1) 식당 존재 여부
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException(
                    "해당 식당을 찾을 수 없습니다. restaurantId=" + restaurantId));

        // 2) 메뉴 존재 여부
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException(
                    "해당 메뉴를 찾을 수 없습니다. menuId=" + menuId));

        // 3) 메뉴가 이 식당에 속해있는지 확인
        if (!menu.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("해당 메뉴는 이 식당에 속해있지 않습니다.");
        }

        // 4) 삭제
        menuRepository.delete(menu);

        // 5) 응답 DTO
        return MenuDeleteResponse.builder()
                .status(200)
                .success(true)
                .message("메뉴가 정상적으로 삭제되었습니다.")
                .data(MenuDeleteResponse.Data.builder()
                        .menuId(menuId)
                        .build())
                .build();
    }
}
