package com.kt.hiorder_backend.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDto {
    private Long menuId;
    private String menuName;
    private BigDecimal price;
    private String menuDescription;
    private String menuImageUrl;
    private Integer displayOrder;
}

