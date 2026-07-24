package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.entity.Report;
import com.example.basecamp_server.domain.report.repository.ReportRepository;
import com.example.basecamp_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 💡 로그인한 유저의 제보 목록만 조회
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getMyReports(Long userId) {
        // 1. 유저 존재 유무 검증 (필요한 경우 유지)
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        // 2. userId로 바로 제보 목록 조회 후 DTO 변환
        return reportRepository.findByUserId(userId).stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제보입니다. ID: " + reportId));

        return ReportResponseDto.from(report);
    }
}