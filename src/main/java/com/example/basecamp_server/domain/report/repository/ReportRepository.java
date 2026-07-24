package com.example.basecamp_server.domain.report.repository;

import com.example.basecamp_server.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // userId 필드로 제보 목록 조회
    List<Report> findByUserId(Long userId);
}