package com.kt.hiorder_backend.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerViewLoginResponse {
    private int status;
    private boolean success;
    private Long restaurantId;
    private Long tableId;
    private String message; // 필요 시
}

