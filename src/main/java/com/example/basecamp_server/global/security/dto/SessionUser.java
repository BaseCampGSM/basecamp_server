package com.example.basecamp_server.global.security.dto;

import com.example.basecamp_server.domain.user.entity.Role;
import com.example.basecamp_server.domain.user.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {

    private final Long id; // 👈 id 필드 추가
    private final String name;
    private final String email;
    private final String picture;
    private final Role role;

    public SessionUser(User user) {
        this.id = user.getId(); // 👈 user 엔티티에서 id 가져오기
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.role = user.getRole();
    }
}