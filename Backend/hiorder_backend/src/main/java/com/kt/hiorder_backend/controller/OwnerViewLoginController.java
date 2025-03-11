package com.kt.hiorder_backend.controller;

import com.kt.hiorder_backend.dto.OwnerViewLoginRequest;
import com.kt.hiorder_backend.dto.OwnerViewLoginResponse;
import com.kt.hiorder_backend.service.OwnerViewLoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/ownerView")
public class OwnerViewLoginController {

    private final OwnerViewLoginService loginService;

    public OwnerViewLoginController(OwnerViewLoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> ownerViewLogin(@RequestBody OwnerViewLoginRequest request) {
        try {
            OwnerViewLoginResponse response = loginService.login(request);
            return ResponseEntity.ok(response);  // 200 OK
        } catch (IllegalArgumentException e) {
            // 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    createErrorResponse(400, "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다.")
            );
        } catch (RuntimeException e) {
            // 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse(404, "입력하신 레스토랑 이름이 올바르지 않습니다.")
            );
        } catch (Exception e) {
            // 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createErrorResponse(500, "서버 내부 문제로 요청을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.")
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

