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
    private List<RecommendationDto> recommendations;
    private List<SourceDto> sources; // 💡 단순 문자열 목록 -> 객체 배열로 변경
    private LocalDateTime created_at;

    /**
     * 추천 시설/정책 DTO
     */
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

    /**
     * 💡 출처 정보 DTO (title, org, url, updated_at)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDto {
        private String title;
        private String org;
        private String url;
        private String updated_at; // ISO 8601 포맷 문자열
    }

    public static ReportResponseDto from(Report report) {
        if (report == null) return null;

        return ReportResponseDto.builder()
                .report_id(report.getId())
                .text(report.getContent() != null ? report.getContent() : "")
                .category(report.getCategory())
                .status(report.getStatus() != null ? report.getStatus() : "received")
                .urgency(report.getUrgency())
                .solution(report.getSolution())
                .recommendations(Collections.emptyList()) // 추천 데이터 없을 시 []
                .sources(Collections.emptyList())         // 출처 데이터 없을 시 []
                .created_at(report.getCreatedAt())
                .build();
    }
}