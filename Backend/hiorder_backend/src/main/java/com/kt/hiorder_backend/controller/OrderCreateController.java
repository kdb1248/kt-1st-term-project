package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.OrderCreateRequest;
import com.kt.hiorder_backend.dto.OrderCreateResponse;
import com.kt.hiorder_backend.service.OrderCreateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class OrderCreateController {

    private final OrderCreateService orderCreateService;

    public OrderCreateController(OrderCreateService orderCreateService) {
        this.orderCreateService = orderCreateService;
    }

    @PostMapping("/{restaurantId}/tables/{tableId}/orders")
    public ResponseEntity<?> createOrder(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("tableId") Long tableId,
        @RequestBody OrderCreateRequest request
    ) {
        try {
            OrderCreateResponse response = orderCreateService.createOrder(restaurantId, tableId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
        } catch (IllegalArgumentException e) {
            // 400
            return ResponseEntity.badRequest().body(createErrorResponse(400, "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다."));
        } catch (RuntimeException e) {
            // 404 (식당, 테이블 없음) 등
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

