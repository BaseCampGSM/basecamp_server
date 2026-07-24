package com.example.basecamp_server.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSolveRequestDto {
    private String location;
    private String user_query; // AI 서버 JSON 키 이름에 맞춰 user_query 사용
}