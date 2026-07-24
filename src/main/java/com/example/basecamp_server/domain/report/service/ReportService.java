package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.ai.dto.AiSolveResponseDto;
import com.example.basecamp_server.domain.ai.service.AiService;
import com.example.basecamp_server.domain.report.dto.request.ReportCreateRequestDto;
import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.entity.Report;
import com.example.basecamp_server.domain.report.repository.ReportRepository;
import com.example.basecamp_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AiService aiService; // 💡 AI 서비스 주입 추가

    /**
     * 제보 접수 (생성 + AI 분석 연동)
     */
    @Transactional
    public Long createReport(Long userId, ReportCreateRequestDto dto) {
        // 1. 기본 제보 엔티티 저장
        Report report = Report.builder()
                .content(dto.getText())
                .address(dto.getAddress())
                .latitude(dto.getLat())
                .longitude(dto.getLng())
                .userId(userId)
                .build();

        Report savedReport = reportRepository.save(report);

        // 2. 💡 AI 분석 API 호출 및 데이터 업데이트
        try {
            AiSolveResponseDto aiResult = aiService.solveIssue(dto.getAddress(), dto.getText());
            if (aiResult != null) {
                savedReport.updateAnalysisResult(
                        aiResult.getCategory(),
                        aiResult.getUrgency(),
                        aiResult.getSolution()
                );
            }
        } catch (Exception e) {
            log.error("AI 서버 분석 실패: {}", e.getMessage());
            // AI 실패 시에도 제보 자체는 'received' 상태로 유지
        }

        return savedReport.getId();
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