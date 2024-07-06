package com.gamegoo.controller.report;


import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.report.Report;
import com.gamegoo.dto.report.ReportRequest;
import com.gamegoo.dto.report.ReportResponse;
import com.gamegoo.service.report.ReportService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "신고 관련 API")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("")
    public ApiResponse<ReportResponse.reportInsertResultDTO> reportInsert(
            @RequestBody @Valid ReportRequest.reportInsertDTO request
    ){
        Long memberId = JWTUtil.getCurrentUserId();

        Report report = reportService.insertReport(request,memberId);

        List<Long> reportTypeIdList = report.getReportTypeMappingList().stream()
                .map(reportTypeMapping -> reportTypeMapping.getReportType().getId())
                .collect(Collectors.toList());

        ReportResponse.reportInsertResultDTO result = ReportResponse.reportInsertResultDTO.builder()
                .targetId(report.getTarget().getId())
                .reportId(report.getId())
                .contents(report.getReportContent())
                .reportTypeIdList(reportTypeIdList)
                .build();

        return ApiResponse.onSuccess(result);
    }
}
