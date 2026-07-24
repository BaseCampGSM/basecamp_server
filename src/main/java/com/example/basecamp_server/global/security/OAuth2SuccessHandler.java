package com.example.basecamp_server.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 요청 헤더(Referer/Origin) 확인
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");

        // 2. 기본값은 Vercel 배포 주소로 설정
        String frontendBaseUrl = "https://basecampclient.vercel.app";

//        // 3. 로컬 요청인 경우 로컬 주소로 변경
//        if ((referer != null && referer.contains("localhost")) || (origin != null && origin.contains("localhost"))) {
//            frontendBaseUrl = "http://localhost:3000";
//        }

        // 4. 최종 targetUrl 생성 (/callback?status=success)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/callback")
                .queryParam("status", "success")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}