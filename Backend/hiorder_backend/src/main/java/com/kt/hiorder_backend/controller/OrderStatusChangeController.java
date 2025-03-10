package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.OrderStatusChangeResponse;
import com.kt.hiorder_backend.service.OrderStatusChangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class OrderStatusChangeController {

    private final OrderStatusChangeService changeService;

    public OrderStatusChangeController(OrderStatusChangeService changeService) {
        this.changeService = changeService;
    }

    // ex) PUT /restaurants/{restaurantId}/tables/{tableId}/orders/changeStatus
    @PutMapping("/{restaurantId}/tables/{tableId}/orders/changeStatus")
    public ResponseEntity<?> changeOrderStatus(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("tableId") Long tableId
    ) {
        try {
            OrderStatusChangeResponse response = changeService.changeOrderStatus(restaurantId, tableId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    createErrorResponse(400, "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다.")
            );
        } catch (RuntimeException e) {
            // 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            // 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(500, "서버 내부 오류가 발생했습니다.")
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
