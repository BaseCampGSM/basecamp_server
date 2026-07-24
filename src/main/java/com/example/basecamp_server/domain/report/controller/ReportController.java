package com.example.basecamp_server.domain.report.controller;

import com.example.basecamp_server.domain.report.dto.request.ReportCreateRequestDto;
import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.service.ReportService;
import com.example.basecamp_server.global.security.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final HttpSession httpSession;

    /**
     * 제보 접수 API
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody ReportCreateRequestDto dto) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        Long reportId = reportService.createReport(user.getId(), dto);
        return ResponseEntity.ok(Map.of("report_id", String.valueOf(reportId), "status", "received"));
    }

    /**
     * 내 제보 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getMyReports() {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<ReportResponseDto> reports = reportService.getMyReports(user.getId());
        return ResponseEntity.ok(reports);
    }

    /**
     * 제보 상세 조회 API
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long reportId) {
        ReportResponseDto report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }
}