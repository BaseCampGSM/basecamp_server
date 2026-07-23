package com.example.basecamp_server.domain.report.dto.response;

import com.example.basecamp_server.domain.report.entity.Report;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ReportResponseDto {

    @JsonProperty("report_id")
    private final String reportId;

    private final String text;
    private final String category;
    private final String urgency;
    private final String solution;
    private final String status;
    private final List<Object> sources;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    public ReportResponseDto(Report report) {
        this.reportId = String.valueOf(report.getId());
        this.text = report.getContent();
        this.category = report.getCategory();
        this.urgency = report.getUrgency();
        this.solution = report.getSolution();
        this.status = report.getStatus() != null ? report.getStatus() : "received";
        this.sources = new ArrayList<>();
        this.createdAt = report.getCreatedAt();
    }
}