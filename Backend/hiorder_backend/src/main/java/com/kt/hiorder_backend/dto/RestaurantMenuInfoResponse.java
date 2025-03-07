package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestaurantMenuInfoResponse {
    private int status;          // 200 등
    private boolean success;     // true/false
    private List<MenuDto> data;  // 메뉴 목록
    private String message;      // 예: "메뉴 조회에 성공했습니다."
}

