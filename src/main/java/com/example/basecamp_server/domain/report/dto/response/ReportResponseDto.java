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
    private String urgency;             // 💡 추가
    private String solution;            // 💡 추가
    private List<String> sources;       // 💡 추가
    private LocalDateTime created_at;

    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .report_id(report.getId())
                .text(report.getContent())
                .category(report.getCategory())
                .status(report.getStatus())
                .urgency(report.getUrgency())
                .solution(report.getSolution())
                .sources(Collections.emptyList()) // 엔티티에 sources가 따로 없다면 빈 배열 반환하여 프론트 404/Crash 방지
                .created_at(report.getCreatedAt())
                .build();
    }
}