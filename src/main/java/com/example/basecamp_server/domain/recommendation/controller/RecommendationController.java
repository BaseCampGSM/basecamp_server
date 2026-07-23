package com.example.basecamp_server.domain.recommendation.controller;

import com.example.basecamp_server.domain.recommendation.dto.response.RecommendationResponseDto;
import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class RecommendationController {

    private final ReportService reportService;

    /** 3-4. 추천·지도용 데이터 GET /api/v1/recommendations?report_id={id} */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendations(@RequestParam("report_id") Long reportId) {
        ReportResponseDto report = reportService.getReport(reportId);

        // 해당 신고의 위치/카테고리 기반 추천 시설 목록 구성 (예시 데이터 구조)
        RecommendationResponseDto recommendation = RecommendationResponseDto.builder()
                .name(report.getCategory() + " 관할 기관")
                .category(report.getCategory())
                .lat(37.4842)
                .lng(126.9297)
                .address("관할 지자체 안내센터")
                .sourceUrl("https://www.data.go.kr")
                .build();

        return ResponseEntity.ok(List.of(recommendation));
    }
}