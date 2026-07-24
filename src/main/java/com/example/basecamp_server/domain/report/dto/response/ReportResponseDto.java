package com.example.basecamp_server.domain.report.dto.response;

import com.example.basecamp_server.domain.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {

    private Long report_id;
    private String text;
    private String category;
    private String status;
    private LocalDateTime created_at;

    // 💡 정적 팩토리 메서드 from() 추가
    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .report_id(report.getId())
                .text(report.getContent()) // 👈 getText() 대신 getContent() 사용
                .category(report.getCategory())
                .status(report.getStatus())
                .created_at(report.getCreatedAt())
                .build();
    }
}