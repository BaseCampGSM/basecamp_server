package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.report.dto.request.ClaudeRequestDto;
import com.example.basecamp_server.domain.report.dto.response.ClaudeResponseDto;
import com.example.basecamp_server.domain.report.dto.response.ReportAnalysisResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClaudeAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // 생성해둔 ObjectMapperConfig 빈 주입받기

    @Value("${api.anthropic.key}")
    private String anthropicApiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    public ReportAnalysisResponseDto analyzeReport(String reportText) {
        String prompt = """
            다음 제보 내용을 분석해서 JSON 형식으로만 응답해줘. 다른 서론이나 설명은 절대 포함하지 마.
            
            [제보 내용]: %s
            
            [응답 형식]:
            {
              "category": "제보 유형 (예: 도로/교통, 시설물, 환경, 안전 등)",
              "urgency": "긴급도 (HIGH, MEDIUM, LOW 중 하나)",
              "summary": "제보 내용 1~2문장 요약",
              "solution": "초기 대응 및 권장 해결 방안"
            }
            """.formatted(reportText);

        ClaudeRequestDto requestDto = ClaudeRequestDto.builder()
                .model("claude-3-5-sonnet-20241022")
                .max_tokens(1000)
                .messages(List.of(
                        ClaudeRequestDto.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<ClaudeRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<ClaudeResponseDto> response = restTemplate.exchange(
                    CLAUDE_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    ClaudeResponseDto.class
            );

            if (response.getBody() != null && !response.getBody().getContent().isEmpty()) {
                String rawText = response.getBody().getContent().get(0).getText();
                return objectMapper.readValue(rawText, ReportAnalysisResponseDto.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Claude API 분석 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return new ReportAnalysisResponseDto("미분류", "LOW", "분석 실패", "기본 대응 지침 확인 필요");
    }
}