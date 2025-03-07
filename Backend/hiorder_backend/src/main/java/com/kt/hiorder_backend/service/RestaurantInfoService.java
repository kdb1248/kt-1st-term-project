package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.RestaurantInfoResponse;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

@Service
public class RestaurantInfoService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;

    public RestaurantInfoService(RestaurantRepository restaurantRepository,
                                 RestaurantTablesRepository restaurantTablesRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
    }

    public RestaurantInfoResponse getRestaurantInfo(Long restaurantId, Long tableId) {
        // 1) 식당 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당이 존재하지 않습니다."));

        // 2) 테이블 조회
        RestaurantTables table = restaurantTablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("해당 테이블이 존재하지 않습니다."));

        // 3) 테이블이 해당 식당에 속하는지 확인 (예: table.getRestaurant().getRestaurantId() == restaurantId)
        if (!table.getRestaurant().getRestaurantId().equals(restaurantId)) {
            // 식당id 불일치
            throw new RuntimeException("테이블이 해당 식당에 속해있지 않습니다.");
        }

        // 4) 응답 생성
        return RestaurantInfoResponse.builder()
                .status(200)
                .success(true)
                .restaurantName(restaurant.getRestaurantName())
                .tableName(table.getTableName())
                .build();
    }
}