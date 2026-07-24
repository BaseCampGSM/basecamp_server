package com.example.basecamp_server.domain.report.controller;

import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.service.ReportService;
import com.example.basecamp_server.global.security.dto.SessionUser; // 세션 사용 시
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final HttpSession httpSession; // 💡 세션 로그인 기준

    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getMyReports() {
        // 1. 세션에서 현재 로그인된 유저 가져오기
        SessionUser user = (SessionUser) httpSession.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401).build(); // 미인증 시 401 UnAuthorized
        }

        // 2. 로그인된 유저의 제보만 가져와서 반환
        List<ReportResponseDto> reports = reportService.getMyReports(user.getId());
        return ResponseEntity.ok(reports);
    }
}