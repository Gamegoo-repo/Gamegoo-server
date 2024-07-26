package com.gamegoo.controller.report;


import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.report.Report;
import com.gamegoo.dto.report.ReportRequest;
import com.gamegoo.dto.report.ReportResponse;
import com.gamegoo.service.report.ReportService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/reports")
@Tag(name = "Reports", description = "신고 관련 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("")
    @Operation(summary = "회원 신고 API", description = "대상 회원을 신고하는 API 입니다.")
    public ApiResponse<ReportResponse.reportInsertResponseDTO> reportInsert(
        @RequestBody @Valid ReportRequest.reportInsertDTO request
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Report report = reportService.insertReport(request, memberId);

        List<Long> reportTypeIdList = report.getReportTypeMappingList().stream()
            .map(reportTypeMapping -> reportTypeMapping.getReportType().getId())
            .collect(Collectors.toList());

        ReportResponse.reportInsertResponseDTO result = ReportResponse.reportInsertResponseDTO.builder()
            .targetId(report.getTarget().getId())
            .reportId(report.getId())
            .contents(report.getReportContent())
            .reportTypeIdList(reportTypeIdList)
            .build();

        return ApiResponse.onSuccess(result);
    }
}
