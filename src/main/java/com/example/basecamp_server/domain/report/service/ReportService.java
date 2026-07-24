package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.report.dto.request.ReportCreateRequestDto;
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

    /**
     * 제보 접수 (생성)
     */
    @Transactional
    public Long createReport(Long userId, ReportCreateRequestDto dto) {
        Report report = Report.builder()
                .content(dto.getText())
                .address(dto.getAddress())
                .latitude(dto.getLat())
                .longitude(dto.getLng())
                .userId(userId)
                .build();
        return reportRepository.save(report).getId();
    }

    /**
     * 내 제보 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getMyReports(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        return reportRepository.findByUserId(userId).stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 제보 단건 상세 조회
     */
    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제보입니다. ID: " + reportId));

        return ReportResponseDto.from(report);
    }
}