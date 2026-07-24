package com.example.basecamp_server.domain.ai.service;

import com.example.basecamp_server.domain.ai.dto.AiSolveRequestDto;
import com.example.basecamp_server.domain.ai.dto.AiSolveResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiService {

    private final RestClient restClient;
    private static final String AI_BASE_URL = "https://basecamp-imsi.p-e.kr";

    public AiService() {
        this.restClient = RestClient.builder()
                .baseUrl(AI_BASE_URL)
                .build();
    }

    /**
     * AI 이슈 해결 API 호출
     */
    public AiSolveResponseDto solveIssue(String location, String userQuery) {
        AiSolveRequestDto request = AiSolveRequestDto.builder()
                .location(location)
                .user_query(userQuery)
                .build();

        return restClient.post()
                .uri("/api/ai/solve-issue")
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(AiSolveResponseDto.class);
    }

    /**
     * AI 서버 상태 체크 (필요 시 활용)
     */
    public String checkStatus() {
        return restClient.get()
                .uri("/api/status")
                .retrieve()
                .body(String.class);
    }
}