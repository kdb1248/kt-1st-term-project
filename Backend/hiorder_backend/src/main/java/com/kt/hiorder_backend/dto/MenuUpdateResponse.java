package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuUpdateResponse {
    private int status;       // 예: 200
    private boolean success;  // 예: true
    private Data data;
    private String message;   // 예: "메뉴가 수정되었습니다."

    @Getter
    @Builder
    public static class Data {
        private Long menuId;
        private Long menuCategoryId;
        private String menuName;        // 수정 후 최종 한글 메뉴명
        private String menuDescription; // 수정 후 최종 한글 메뉴 설명
        private String menuImageUrl;
        private Long price;            // or BigDecimal
    }
}
