package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.MenuDeleteResponse;
import com.kt.hiorder_backend.service.MenuDeleteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurants")
@CrossOrigin(origins = "http://localhost:5173")
public class MenuDeleteController {

    private final MenuDeleteService menuDeleteService;

    public MenuDeleteController(MenuDeleteService menuDeleteService) {
        this.menuDeleteService = menuDeleteService;
    }

    @DeleteMapping("/{restaurantId}/menus/{menuId}")
    public ResponseEntity<?> deleteMenu(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("menuId") Long menuId
    ) {
        try {
            MenuDeleteResponse response = menuDeleteService.deleteMenu(restaurantId, menuId);
            return ResponseEntity.ok(response);

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
