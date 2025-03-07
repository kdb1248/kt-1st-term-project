package com.kt.hiorder_backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.kt.hiorder_backend.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // 식당 이름으로 식당 조회
    Optional<Restaurant> findByRestaurantName(String restaurantName);
}

