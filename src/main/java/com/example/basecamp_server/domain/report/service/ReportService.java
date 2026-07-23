package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.report.dto.request.ReportCreateRequestDto;
import com.example.basecamp_server.domain.report.dto.response.ReportAnalysisResponseDto;
import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.entity.Report;
import com.example.basecamp_server.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ClaudeAnalysisService claudeAnalysisService;

    /**
     * 1. 신고 생성 (text, lat, lng, address 매핑)
     */
    @Transactional
    public ReportResponseDto createReport(ReportCreateRequestDto requestDto) {
        Report report = Report.builder()
                .content(requestDto.getText())
                .address(requestDto.getAddress())
                .latitude(requestDto.getLat())
                .longitude(requestDto.getLng())
                .build();

        Report savedReport = reportRepository.save(report);
        return new ReportResponseDto(savedReport);
    }

    /**
     * 2. 신고 단건 상세 조회
     */
    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다. id=" + reportId));
        return new ReportResponseDto(report);
    }

    /**
     * 3. 신고 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportResponseDto::new)
                .toList();
    }

    /**
     * 4. AI 분석 수행 및 분석 결과 업데이트 (category, urgency, solution)
     */
    @Transactional
    public ReportResponseDto analyzeReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다. id=" + reportId));

        // Claude AI 분석 호출
        ReportAnalysisResponseDto analysisResult = claudeAnalysisService.analyzeReport(report.getContent());

        // 분석 결과(카테고리, 긴급도, 해결책) 엔티티에 업데이트
        report.updateAnalysisResult(
                analysisResult.getCategory(),
                analysisResult.getUrgency(),
                analysisResult.getSolution()
        );

        return new ReportResponseDto(report);
    }
}