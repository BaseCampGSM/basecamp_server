package com.example.basecamp_server.domain.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // text

    private String category;
    private String urgency;

    @Column(columnDefinition = "TEXT")
    private String solution;

    private String status;
    private String address;
    private Double latitude;
    private Double longitude;

    private Long userId;
    private LocalDateTime createdAt;

    @Builder
    public Report(String content, String address, Double latitude, Double longitude, Long userId) {
        this.content = content;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.status = "received";
        this.createdAt = LocalDateTime.now();
    }

    public void updateAnalysisResult(String category, String urgency, String solution) {
        this.category = category;
        this.urgency = urgency;
        this.solution = solution;
        this.status = "analyzed";
    }
}