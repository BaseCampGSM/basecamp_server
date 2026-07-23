package com.example.basecamp_server.domain.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportAnalysisResponseDto {
    private String category;   // 제보 유형 (예: 도로 파손, 시설물 고장 등)
    private String urgency;    // 긴급도 (HIGH, MEDIUM, LOW)
    private String summary;    // 요약
    private String solution;   // RAG 근거 기반 기본 해결 방안
}