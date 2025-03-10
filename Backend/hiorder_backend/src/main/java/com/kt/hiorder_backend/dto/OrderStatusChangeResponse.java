package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderStatusChangeResponse {
    private int status;              // 200
    private boolean success;         // true
    private Data data;               // 변경된 주문 수 등
    private String message;          // ex) "해당 테이블의 PENDING 주문 3건을 PAID로 변경했습니다."

    @Getter
    @Builder
    public static class Data {
        private int updatedCount;        // 몇 건 변경
        private List<Long> updatedOrderIds; // 어떤 주문들이 변경됐는지
    }
}
