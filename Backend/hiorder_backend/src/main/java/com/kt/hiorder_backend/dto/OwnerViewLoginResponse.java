
package com.kt.hiorder_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerViewLoginResponse {
    private int status;        // 200
    private boolean success;   // true
    private Long restaurantId; // 사장님 식당 id
    private String message;    // 필요 시
}

