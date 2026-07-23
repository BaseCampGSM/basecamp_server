package com.example.basecamp_server.domain.report.controller;

import com.example.basecamp_server.domain.report.dto.response.ReportAnalysisResponseDto;
import com.example.basecamp_server.domain.report.service.ClaudeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@RestController
public class ReportAnalysisController {

    private final ClaudeAnalysisService claudeAnalysisService;

    /**
     * 제보 내용 AI 분석 API
     * POST /api/v1/reports/analyze
     * Body: { "content": "신림역 3번 출구 앞 보도블럭이 심하게 깨져있어서 사람들이 넘어질 위험이 있습니다." }
     */
    @PostMapping("/analyze")
    public ResponseEntity<ReportAnalysisResponseDto> analyzeReport(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        ReportAnalysisResponseDto result = claudeAnalysisService.analyzeReport(content);
        return ResponseEntity.ok(result);
    }
}