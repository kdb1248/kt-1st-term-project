package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.OwnerViewLoginRequest;
import com.kt.hiorder_backend.dto.OwnerViewLoginResponse;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnerViewLoginService {

    private final RestaurantRepository restaurantRepository;

    public OwnerViewLoginService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public OwnerViewLoginResponse login(OwnerViewLoginRequest request) {
        // 1) 파라미터 검증
        if (request.getRestaurantName() == null || request.getRestaurantName().isEmpty()) {
            throw new IllegalArgumentException("레스토랑 이름은 필수입니다.");
        }

        // 2) 레스토랑 조회
        Restaurant restaurant = restaurantRepository.findByRestaurantName(request.getRestaurantName())
                .orElseThrow(() -> new RuntimeException("해당 레스토랑이 존재하지 않습니다."));

        // 3) 정상적으로 찾았으면 응답 생성
        return OwnerViewLoginResponse.builder()
                .status(200)
                .success(true)
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }
}

