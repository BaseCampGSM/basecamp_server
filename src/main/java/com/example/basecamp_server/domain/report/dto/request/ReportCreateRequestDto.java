package com.example.basecamp_server.domain.report.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequestDto {
    private String text;
    private Double lat;
    private Double lng;
    private String address;
}