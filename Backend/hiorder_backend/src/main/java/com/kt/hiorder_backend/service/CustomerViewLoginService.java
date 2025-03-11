package com.kt.hiorder_backend.service;



import com.kt.hiorder_backend.dto.CustomerViewLoginRequest;
import com.kt.hiorder_backend.dto.CustomerViewLoginResponse;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerViewLoginService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;

    public CustomerViewLoginService(RestaurantRepository restaurantRepository,
                                    RestaurantTablesRepository restaurantTablesRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
    }

    public CustomerViewLoginResponse login(CustomerViewLoginRequest request) {
        // 1) 파라미터 검증
        if (request.getRestaurantName() == null || request.getRestaurantName().isEmpty() ||
            request.getTableName() == null || request.getTableName().isEmpty()) {
            // 직접 예외 던지거나, null 처리
            throw new IllegalArgumentException("레스토랑 이름과 테이블 이름은 필수입니다.");
        }

        // 2) 레스토랑 조회
        Restaurant restaurant = restaurantRepository.findByRestaurantName(request.getRestaurantName())
                .orElseThrow(() -> new RuntimeException("해당 레스토랑이 존재하지 않습니다."));

        // 3) 테이블 조회
        RestaurantTables restaurantTables = restaurantTablesRepository
                .findByRestaurant_RestaurantIdAndTableName(restaurant.getRestaurantId(), request.getTableName())
                .orElseThrow(() -> new RuntimeException("해당 테이블이 존재하지 않습니다."));

        // 4) 정상적으로 찾았으면 응답 생성
        return CustomerViewLoginResponse.builder()
                .status(200)
                .success(true)
                .restaurantId(restaurant.getRestaurantId())
                .tableId(restaurantTables.getTableId())
                .build();
    }
}

