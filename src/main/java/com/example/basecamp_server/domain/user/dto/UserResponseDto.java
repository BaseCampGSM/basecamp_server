package com.example.basecamp_server.domain.user.dto;

import com.example.basecamp_server.global.security.dto.SessionUser;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final String name;
    private final String email;
    private final String picture;
    private final String role;

    public UserResponseDto(SessionUser sessionUser) {
        this.name = sessionUser.getName();
        this.email = sessionUser.getEmail();
        this.picture = sessionUser.getPicture();
        this.role = sessionUser.getRole();
    }
}