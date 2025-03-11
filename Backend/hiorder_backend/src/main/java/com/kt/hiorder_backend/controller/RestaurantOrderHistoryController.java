package com.kt.hiorder_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.hiorder_backend.dto.RestaurantOrderHistoryResponse;
import com.kt.hiorder_backend.service.RestaurantOrderHistoryService;


// 프론트엔드와 연결
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class RestaurantOrderHistoryController {

    private final RestaurantOrderHistoryService historyService;

    public RestaurantOrderHistoryController(RestaurantOrderHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/{restaurantId}/orders/history")
    public ResponseEntity<?> getRestaurantOrderHistory(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam(name="orderStatus", required=false, defaultValue="PENDING") String orderStatus,
            @RequestParam(name="orderCode", required=false, defaultValue="desc") String orderCode
    ) {
        try {
            RestaurantOrderHistoryResponse response = historyService.getRestaurantOrderHistory(restaurantId, orderStatus, orderCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 400
            return ResponseEntity.badRequest().body(createErrorResponse(400, "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다."));
        } catch (RuntimeException e) {
            // 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(404, e.getMessage()));
        } catch (Exception e) {
            // 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(500, "서버 내부 오류가 발생했습니다."));
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
