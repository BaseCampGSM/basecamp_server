package com.example.basecamp_server.domain.report.dto.response;

import com.example.basecamp_server.domain.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {

    private Long report_id;
    private String text;
    private String category;
    private String status;
    private String urgency;
    private String solution;

    // 💡 문자열 목록 대신 장소 객체 DTO 목록으로 변경
    private List<RecommendationDto> recommendations;
    private List<String> sources;
    private LocalDateTime created_at;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationDto {
        private String name;
        private String category;
        private Double lat;
        private Double lng;
        private String address;
        private String source_url;
    }

    public static ReportResponseDto from(Report report) {
        if (report == null) return null;

        // 💡 AI 연동 전 혹은 좌표/장소 정보가 없을 때는 null 대신 안전하게 빈 배열([]) 반환
        List<RecommendationDto> recommendationList = Collections.emptyList();

        return ReportResponseDto.builder()
                .report_id(report.getId())
                .text(report.getContent() != null ? report.getContent() : "")
                .category(report.getCategory())
                .status(report.getStatus() != null ? report.getStatus() : "received")
                .urgency(report.getUrgency())
                .solution(report.getSolution())
                .recommendations(recommendationList) // 빈 배열([])로 전달하여 프론트 UI 파싱 에러 방지
                .sources(Collections.emptyList())
                .created_at(report.getCreatedAt())
                .build();
    }
}