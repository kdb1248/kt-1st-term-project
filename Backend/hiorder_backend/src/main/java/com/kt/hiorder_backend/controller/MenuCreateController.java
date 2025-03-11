package com.kt.hiorder_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.hiorder_backend.dto.MenuCreateRequest;
import com.kt.hiorder_backend.dto.MenuCreateResponse;
import com.kt.hiorder_backend.service.MenuCreateService;



@RestController
@RequestMapping("/restaurants")
@CrossOrigin(origins = "http://localhost:5173")
public class MenuCreateController {

    private final MenuCreateService menuCreateService;

    public MenuCreateController(MenuCreateService menuCreateService) {
        this.menuCreateService = menuCreateService;
    }

    @PostMapping("/{restaurantId}/menus")
    public ResponseEntity<?> createMenu(
        @PathVariable("restaurantId") Long restaurantId,  // [CHANGED]
        @RequestBody MenuCreateRequest request) 
    
    {
        try {
              // [CHANGED] pass restaurantId separately
              MenuCreateResponse response = menuCreateService.createMenu(restaurantId, request);
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

    // 에러 응답용 JSON
    private Object createErrorResponse(int status, String message) {
        return new Object() {
            public final int statusCode = status;
            public final boolean success = false;
            public final String msg = message;
        };
    }
}

