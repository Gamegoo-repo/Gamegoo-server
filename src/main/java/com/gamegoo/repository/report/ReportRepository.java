package com.gamegoo.repository.report;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndTarget(Member reporter, Member target);
}
