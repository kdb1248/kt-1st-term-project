package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantInfoResponse {
    private int status;
    private boolean success;
    private String restaurantName;
    private String tableName;
    // 필요하다면 추가 필드 (ex. message, etc.)
}