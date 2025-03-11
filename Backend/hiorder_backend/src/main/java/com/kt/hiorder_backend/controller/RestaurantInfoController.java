package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.RestaurantInfoResponse;
import com.kt.hiorder_backend.service.RestaurantInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 프론트엔드와 연결
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class RestaurantInfoController {

    private final RestaurantInfoService restaurantInfoService;

    public RestaurantInfoController(RestaurantInfoService restaurantInfoService) {
        this.restaurantInfoService = restaurantInfoService;
    }

    @GetMapping("/{restaurantId}/tables/{tableId}")
    public ResponseEntity<?> getRestaurantInfo(@PathVariable("restaurantId") Long restaurantId,
                                               @PathVariable("tableId") Long tableId) {
        try {
            // Service 호출
            RestaurantInfoResponse response = restaurantInfoService.getRestaurantInfo(restaurantId, tableId);
            return ResponseEntity.ok(response); // 200 OK
        } catch (RuntimeException e) {
            // 404 Not Found 등 상황에 따라
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            // 그 외 예기치 못한 에러 → 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(500, "서버 내부 문제로 요청을 처리할 수 없습니다.")
            );
        }
    }

    private Object createErrorResponse(int status, String message) {
        return new Object() {
            public final int statusCode = status;
            public final boolean success = false;
            public final String msg = message;
        };
    }
}

