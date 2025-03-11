package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RestaurantTableInfoResponse {
    private int status;            // 200
    private boolean success;       // true
    private List<TableDto> data;   // 테이블 목록
    private String message;        // "테이블 목록 조회에 성공했습니다."

    @Getter
    @Builder
    public static class TableDto {
        private Long tableId;
        private String tableName;
    }
}
