package com.kt.hiorder_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kt.hiorder_backend.entity.RestaurantTables;

import java.util.Optional;

public interface RestaurantTablesRepository extends JpaRepository<RestaurantTables, Long> {

    // 특정 식당 + 테이블 이름으로 조회
    Optional<RestaurantTables> findByRestaurant_RestaurantIdAndTableName(Long restaurantId, String tableName);
}
