package com.gamegoo.service.member;

import com.gamegoo.domain.report.ReportType;
import com.gamegoo.repository.member.ReportTypeRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Service
@Transactional
public class ReportTypeService {
    private final ReportTypeRepository reportTypeRepository;

    public ReportTypeService(ReportTypeRepository reportTypeRepository) {
        this.reportTypeRepository = reportTypeRepository;
    }

    public ReportType getReportTypeById(Long id) {
        return reportTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReportType with id " + id + " not found"));
    }
}
