package com.kt.hiorder_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private String orderTable;
    private List<OrderItemRequest> orderItems;
    private Long totalAmount; // ex) 26000

    @Getter
    @Setter
    public static class OrderItemRequest {
        private String menuName;
        private int quantity;
        private BigDecimal itemPrice;
        private Long menuId;
        // (실무에선 menuId도 받으면 안전)
    }
}

