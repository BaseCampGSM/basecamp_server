package com.example.basecamp_server.domain.report.repository;

import com.example.basecamp_server.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}