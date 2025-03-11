package com.kt.hiorder_backend.dto;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderCreateResponse {
    private int status;           // 201
    private boolean success;      // true
    private Data data;
    private String message;       // "주문이 성공적으로 생성되었습니다."

    @Getter
    @Builder
    public static class Data {
        private Long orderId;
        private String orderStatus;   // ex) PENDING
        private String orderTable;     // "23번 테이블"
        private Long tableId;         // 23
        private Integer orderCode;  // <--- 추가
        private Long totalAmount;     // 26000
        private LocalDateTime createdAt;
        private List<OrderItemDto> orderItems;
    }

    @Getter
    @Builder
    public static class OrderItemDto {
        private Long orderItemId;
        private Long menuId;        // 추가
        private String menuName;
        private int quantity;
        private BigDecimal itemPrice;
    }
}

