package com.example.basecamp_server.domain.user.controller;

import com.example.basecamp_server.domain.user.dto.UserResponseDto;
import com.example.basecamp_server.global.security.annotation.LoginUser;
import com.example.basecamp_server.global.security.dto.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    /**
     * 메인 페이지 접속 시 로그인 상태 확인
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index(@LoginUser SessionUser user) {
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            response.put("status", "success");
            response.put("message", "로그인된 상태입니다.");
            response.put("user", new UserResponseDto(user));
            return ResponseEntity.ok(response);
        }

        response.put("status", "guest");
        response.put("message", "로그인되지 않은 상태입니다.");
        response.put("loginUrl", "/oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인한 내 정보 조회 API
     */
    @GetMapping("/api/v1/users/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@LoginUser SessionUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserResponseDto(user));
    }
}