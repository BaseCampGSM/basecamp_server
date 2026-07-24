package com.example.basecamp_server.domain.report.repository;

import com.example.basecamp_server.domain.report.entity.Report;
import com.example.basecamp_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // User 객체로 내 제보 조회
    List<Report> findByUser(User user);

    // 또는 userId 숫자 필드로 조회 시:
    // List<Report> findByUserId(Long userId);
}