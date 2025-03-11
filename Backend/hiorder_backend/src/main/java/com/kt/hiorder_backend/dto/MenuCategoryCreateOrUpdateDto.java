package com.kt.hiorder_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuCategoryCreateOrUpdateDto {
    private Long menuCategoryId;        // 존재 시 업데이트, null 시 새로 등록?
    private String menuCategoryName;    // 한글
    // ... price, etc. -> not relevant for category
}
