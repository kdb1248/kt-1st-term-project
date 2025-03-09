package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.TableOrderHistoryResponse;
import com.kt.hiorder_backend.service.TableOrderHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 프론트엔드와 연결
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class TableOrderHistoryController {

    private final TableOrderHistoryService historyService;

    public TableOrderHistoryController(TableOrderHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/{restaurantId}/tables/{tableId}/orders/history")
    public ResponseEntity<?> getTableOrderHistory(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("tableId") Long tableId,
            // [CHANGED] QueryParam name = orderStatus
            @RequestParam(name="orderStatus", required=false, defaultValue="PENDING") String orderStatus,
            // [CHANGED] QueryParam name = orderCode
            @RequestParam(name="orderCode", required=false, defaultValue="desc") String orderCode
    ) {
        try {
            TableOrderHistoryResponse response = historyService.getTableOrderHistory(restaurantId, tableId, orderStatus, orderCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(400, "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(404, e.getMessage()));
        } catch (Exception e) {
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

