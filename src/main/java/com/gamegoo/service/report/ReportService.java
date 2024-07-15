package com.gamegoo.service.report;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
import com.gamegoo.apiPayload.exception.handler.ReportHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.report.Report;
import com.gamegoo.domain.report.ReportType;
import com.gamegoo.domain.report.ReportTypeMapping;
import com.gamegoo.dto.report.ReportRequest;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.repository.report.ReportRepository;
import com.gamegoo.repository.report.ReportTypeMappingRepository;
import com.gamegoo.repository.report.ReportTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService{
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ReportTypeMappingRepository reportTypeMappingRepository;

    public Report insertReport(ReportRequest.reportInsertDTO request, Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // target 회원 존재 여부 검증.
        Member targetMember = memberRepository.findById(request.getTargetMemberId()).orElseThrow(()->new MemberHandler(ErrorStatus.REPORT_TARGET_MEMBER_NOT_FOUND));

        // target 회원 탈퇴 여부 검증.
        if (targetMember.getBlind()){
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // reportType이 실제 존재 여부 검증.
        List<ReportType> reportTypeList = new ArrayList<>();
        request.getReportTypeIdList()
                .forEach(reportTypeId -> {
                    ReportType reportType = reportTypeRepository.findById(reportTypeId).orElseThrow(() -> new TempHandler(ErrorStatus._BAD_REQUEST));
                    reportTypeList.add(reportType);
                });

        // member 와 targetMember가 같은 회원인 경우.
        if (member.getId().equals(targetMember.getId())){
            throw new MemberHandler(ErrorStatus.MEMBER_AND_TARGET_MEMBER_SAME);
        }

        // report 엔티티 생성 및 연관관계 매핑.
        Report report = Report.builder()
                .target(targetMember)
                .reportContent(request.getContents())
                .reportTypeMappingList(new ArrayList<>())
                .build();

        report.setReporter(member);
        Report saveReport = reportRepository.save(report);

        // reportTypeMapping 엔티티 생성 및 연관관계 매핑.
        reportTypeList.forEach(reportType -> {
            ReportTypeMapping reportTypeMapping = ReportTypeMapping.builder()
                    .reportType(reportType)
                    .build();

            reportTypeMapping.setReport(saveReport);
            reportTypeMappingRepository.save(reportTypeMapping);
        });

        return saveReport;
    }
}
