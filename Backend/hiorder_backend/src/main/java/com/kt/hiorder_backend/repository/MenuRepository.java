package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 식당ID + 카테고리ID로 메뉴 목록
    List<Menu> findByRestaurant_RestaurantIdAndMenuCategory_MenuCategoryId(Long restaurantId, Long menuCategoryId);

    // 식당ID + 카테고리ID + displayOrder 정렬
    List<Menu> findByRestaurant_RestaurantIdAndMenuCategory_MenuCategoryIdOrderByDisplayOrderAsc(Long restaurantId, Long menuCategoryId);
}


