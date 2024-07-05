package com.gamegoo.controller.member;


import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.ReportHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.report.Report;
import com.gamegoo.dto.member.ReportRequestDTO;
import com.gamegoo.service.member.ReportService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Slf4j
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "회원 신고 API", description = "대상 회원을 신고하는 API 입니다.")
    @PostMapping("/report/{reporterId}")
    public ApiResponse<String> reportMember (
            @PathVariable(name = "reporterId") Long reporterId,
            @RequestParam(name = "targetId") Long targetId,
            @RequestParam(name = "reportTypeId") Long reportTypeId,
            @RequestParam(name = "reportContent", required = false) String reportContent) {

        Long memberId = JWTUtil.getCurrentUserId(); // 헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        Member member = reportService.createReport(reporterId, targetId, reportTypeId, reportContent);

        return ApiResponse.onSuccess("회원 신고 성공");
    }
}


@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createReport(
            @RequestParam String reportContent,
            @RequestParam Long reporterId,
            @RequestParam Long targetId,
            @RequestParam Set<Long> reportTypeIds
    ) {
        // 예시로 MemberId는 단순히 Long 이며 구체적으로는 Member의 id일 수 있다.
        Member reporter = memberService.findById(reporterId);
        Member target = memberService.findById(targetId);

        Report report = reportService.createReport(reportContent, reporter, target, reportTypeIds);

        return ResponseEntity.ok("신고가 성공적으로 등록되었습니다. Report ID: " + report.getId());
    }
}

*/

@RestController
@RequestMapping("/api/reports")
@Slf4j
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/create")
    @Operation(summary = "회원 신고 API", description = "대상 회원을 신고하는 API 입니다.")
    public ApiResponse<String> reportMember(
            @RequestParam(name = "reporterId") Long reporterId,
            @RequestParam(name = "targetId") Long targetId,
            @RequestParam(name = "reportTypeIds") List<Long> reportTypeIds,
            @RequestParam(name = "reportContent", required = false) String reportContent) {

        reportService.createReport(reporterId, targetId, reportTypeIds, reportContent);

        return ApiResponse.onSuccess("회원 신고 성공");
    }
}
