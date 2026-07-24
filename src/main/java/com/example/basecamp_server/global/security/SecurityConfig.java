package com.example.basecamp_server.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. REST API 환경이므로 CSRF disable
                .csrf(csrf -> csrf.disable())

                // 3. URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**", "/oauth2/**", "/login/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                // 4. 💡 구글 OAuth2 로그인 성공 후 리다이렉트 처리
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customAuthenticationSuccessHandler())
                );

        return http.build();
    }

    // 💡 동적 로그인 성공 핸들러
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // 요청을 보낸 프론트엔드의 Referer 또는 Origin 확인
            String referer = request.getHeader("Referer");
            String targetUrl = "https://basecampclient.vercel.app/callback"; // 기본 배포 URL

            // 로컬(localhost:3000)에서 들어온 요청이라면 로컬 callback으로 전송
            if (referer != null && referer.contains("localhost:3000")) {
                targetUrl = "http://localhost:3000/callback";
            }

            response.sendRedirect(targetUrl);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://192.168.*.*:[*]",
                "https://basecampclient.vercel.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}