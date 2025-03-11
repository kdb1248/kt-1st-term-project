package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuCreateResponse {
    private int status;        // 예: 201
    private boolean success;   // 예: true
    private Data data;         // 실제 데이터
    private String message;    // 예: "메뉴가 등록되었습니다."

    @Getter
    @Builder
    public static class Data {
        private Long menuId;
        private Long menuCategoryId;
        // 필요 시 다른 필드도 추가 가능
        // ex) private String menuName;
    }
}
