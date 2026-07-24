package com.example.basecamp_server.domain.report.service;

import com.example.basecamp_server.domain.report.dto.response.ReportResponseDto;
import com.example.basecamp_server.domain.report.repository.ReportRepository;
import com.example.basecamp_server.domain.user.entity.User;
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
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. 해당 유저가 작성한 제보 목록만 DB에서 조회 후 DTO 변환
        return reportRepository.findByUser(user).stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }
}