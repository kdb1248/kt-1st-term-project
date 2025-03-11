package com.kt.hiorder_backend.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuCreateRequest {
    // private Long restaurantId;

    // 카테고리 정보
    private Long menuCategoryId;       // 기존 카테고리 id (존재 시)
    private String menuCategoryName;   // 한글 카테고리명 (새로 등록 or 번역 업데이트)

    // 메뉴 정보
    private String menuName;         // 한글 메뉴명
    private String menuDescription;  // 한글 메뉴 설명
    private BigDecimal price;
    private String menuImageUrl;
}
