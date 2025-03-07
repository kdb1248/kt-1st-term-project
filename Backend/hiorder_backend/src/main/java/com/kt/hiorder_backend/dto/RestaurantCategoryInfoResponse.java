package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestaurantCategoryInfoResponse {
    private int status;          // 예: 200
    private boolean success;     // 예: true
    private List<MenuCategoryDto> data; // 카테고리 목록
    private String message;      // 예: "메뉴 카테고리 조회에 성공했습니다."
}
