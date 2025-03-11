// MenuDeleteResponse.java
package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDeleteResponse {
    private int status;       // 예: 200
    private boolean success;  // 예: true
    private Data data;        // 삭제된 메뉴 정보
    private String message;   // 예: "메뉴가 정상적으로 삭제되었습니다."

    @Getter
    @Builder
    public static class Data {
        private Long menuId;
        // 필요하다면 다른 필드도 가능
    }
}
