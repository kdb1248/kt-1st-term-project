package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.RestaurantMenuInfoResponse;
import com.kt.hiorder_backend.service.RestaurantMenuInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 프론트엔드와 연결
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/restaurants")
public class RestaurantMenuInfoController {

    private final RestaurantMenuInfoService menuInfoService;

    public RestaurantMenuInfoController(RestaurantMenuInfoService menuInfoService) {
        this.menuInfoService = menuInfoService;
    }

    @GetMapping("/{restaurantId}/categories/{menuCategoryId}/menus")
    public ResponseEntity<?> getRestaurantMenus(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("menuCategoryId") Long menuCategoryId,
            @RequestParam(name = "sort", required = false) String sortParam
    ) {
        try {
            RestaurantMenuInfoResponse response =
                    menuInfoService.getMenuList(restaurantId, menuCategoryId, sortParam);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 404 Not Found, etc
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            // 500
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

