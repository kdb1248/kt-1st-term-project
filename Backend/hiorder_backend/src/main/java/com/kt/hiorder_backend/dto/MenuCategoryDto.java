package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuCategoryDto {
    private Long menuCategoryId;
    private String menuCategoryName;
    private Integer displayOrder;
}

