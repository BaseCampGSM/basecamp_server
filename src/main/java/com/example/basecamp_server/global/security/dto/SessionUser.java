package com.example.basecamp_server.global.security.dto;

import com.example.basecamp_server.domain.user.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter // 💡 getId() 메서드를 자동으로 생성해 줍니다.
public class SessionUser implements Serializable {

    private Long id; // 💡 id 필드가 필수입니다.
    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.id = user.getId(); // 💡 User entity에서 id 할당
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}