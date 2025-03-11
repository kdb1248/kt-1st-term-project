package com.kt.hiorder_backend.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuUpdateRequest {
    // 카테고리 정보 (optional)
    private Long menuCategoryId;      // 기존 카테고리 id
    private String menuCategoryName;  // 새 한글 카테고리명 (있으면 카테고리명 수정)

    // 메뉴 정보
    private String menuName;          // 새 한글 메뉴명
    private String menuDescription;   // 새 한글 메뉴 설명
    private BigDecimal price;         // 새 가격
    private String menuImageUrl;      // 새 이미지 URL
}
