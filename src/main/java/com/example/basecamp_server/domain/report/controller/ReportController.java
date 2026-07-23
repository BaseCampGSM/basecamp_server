package com.example.basecamp_server.domain.report.controller;

import com.example.basecamp_server.domain.report.dto.request.ReportCreateRequestDto;
import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class ReportController {

    private final ReportService reportService;

    /** 3-2. 제보 등록 POST /api/v1/reports */
    @PostMapping("/reports")
    public ResponseEntity<ReportResponseDto> createReport(@RequestBody ReportCreateRequestDto requestDto) {
        return ResponseEntity.ok(reportService.createReport(requestDto));
    }

    /** 3-2. 제보 상세 조회 GET /api/v1/reports/{reportId} */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    /** 3-2. 제보 목록 조회 GET /api/v1/reports */
    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponseDto>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /** 3-3. AI 분석 요청 POST /api/v1/analyze */
    @PostMapping("/analyze")
    public ResponseEntity<ReportResponseDto> analyzeReport(@RequestBody Map<String, String> request) {
        Long reportId = Long.parseLong(request.get("report_id"));
        return ResponseEntity.ok(reportService.analyzeReport(reportId));
    }
}