package com.kt.hiorder_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kt.hiorder_backend.dto.MenuUpdateRequest;
import com.kt.hiorder_backend.dto.MenuUpdateResponse;
import com.kt.hiorder_backend.service.MenuUpdateService;

@RestController
@RequestMapping("/restaurants")
@CrossOrigin(origins = "http://localhost:5173")
public class MenuUpdateController {

    private final MenuUpdateService menuUpdateService;

    public MenuUpdateController(MenuUpdateService menuUpdateService) {
        this.menuUpdateService = menuUpdateService;
    }

    @PutMapping("/{restaurantId}/menus/{menuId}")
    public ResponseEntity<?> updateMenu(
        @PathVariable("restaurantId") Long restaurantId,
        @PathVariable("menuId") Long menuId,
        @RequestBody MenuUpdateRequest request
    ) {
        try {
            MenuUpdateResponse response = menuUpdateService.updateMenu(restaurantId, menuId, request);
            return ResponseEntity.status(response.getStatus()).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(400, e.getMessage())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
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
