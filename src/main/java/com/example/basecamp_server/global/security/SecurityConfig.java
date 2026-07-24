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
                // 1. CORS 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. REST API이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 3. API 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**", "/oauth2/**", "/login/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                // 4. 로그인 성공 시 Vercel 콜백 주소로 리다이렉트
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customAuthenticationSuccessHandler())
                );

        return http.build();
    }

    // 💡 [요구사항 2] 로그인 성공 시 백엔드가 프론트엔드로 돌려보내는 주소 고정
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String targetUrl = "https://basecampclient.vercel.app/callback";
            response.sendRedirect(targetUrl);
        };
    }

    // 💡 [요구사항 1] CORS 설정 (Vercel Origin 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // setAllowedOriginPatterns 사용시 문자열 및 와일드카드 지원
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://192.168.*.*:[*]",
                "https://basecampclient.vercel.app", // 👈 끝에 슬래시(/)가 없는지 꼭 확인!
                "https://*.vercel.app"
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