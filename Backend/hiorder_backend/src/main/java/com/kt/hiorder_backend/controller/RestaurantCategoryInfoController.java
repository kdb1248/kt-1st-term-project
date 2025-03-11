package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.RestaurantCategoryInfoResponse;
import com.kt.hiorder_backend.service.RestaurantCategoryInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 프론트엔드와 연결
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class RestaurantCategoryInfoController {

    private final RestaurantCategoryInfoService categoryInfoService;

    public RestaurantCategoryInfoController(RestaurantCategoryInfoService categoryInfoService) {
        this.categoryInfoService = categoryInfoService;
    }

    @GetMapping("/{restaurantId}/categories")
    public ResponseEntity<?> getRestaurantCategories(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam(name = "sort", required = false) String sortParam,
            @RequestParam(name = "lang", required = false, defaultValue = "kr") String lang // [CHANGED]
    ) {
        try {
            RestaurantCategoryInfoResponse response = categoryInfoService.getMenuCategories(restaurantId, sortParam, lang);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 식당 없을 때 등
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(500, "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    private Object createErrorResponse(int status, String errorMsg) {
        return new Object() {
            public final int statusCode = status;
            public final boolean success = false;
            public final String message = errorMsg;
        };
    }
}

