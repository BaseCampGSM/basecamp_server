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
    private List<String> recommendations;
    private List<String> sources;
    private LocalDateTime created_at;

    // 💡 아래 from 메서드 부분을 그대로 덮어씌워주시면 됩니다!
    public static ReportResponseDto from(Report report) {
        if (report == null) return null;

        // solution(해결책)이 null이거나 비어있으면 빈 배열([]), 있으면 배열 형태로 변환
        List<String> recommendationList = (report.getSolution() != null && !report.getSolution().isBlank())
                ? List.of(report.getSolution())
                : Collections.emptyList();

        return ReportResponseDto.builder()
                .report_id(report.getId())
                .text(report.getContent() != null ? report.getContent() : "") // null 방어
                .category(report.getCategory())
                .status(report.getStatus() != null ? report.getStatus() : "received") // null 방어
                .urgency(report.getUrgency())
                .solution(report.getSolution())
                .recommendations(recommendationList)
                .sources(Collections.emptyList())
                .created_at(report.getCreatedAt())
                .build();
    }
}