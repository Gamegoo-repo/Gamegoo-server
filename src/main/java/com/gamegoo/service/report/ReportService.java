package com.gamegoo.service.report;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.ReportHandler;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
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
import java.util.Set;
import java.util.stream.Collectors;

/*
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    private final ReportTypeRepository reportTypeRepository;

    public Member createReport(Long reporterId, Long targetId, Long reportTypeId, String reportContent) {

        // 신고자(member)와 신고 대상(targetMember)을 조회
        Member member = memberRepository.findById(reporterId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetId).orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));
        ReportType reportType = reportTypeRepository.findById(reportTypeId).orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_TYPE_NOT_FOUND));

        // 대상 회원의 탈퇴 여부 검증
        checkBlind(targetMember);

        // 이미 신고한 회원인지 검증
        boolean isReported = reportRepository.existsByReporterAndTarget(member, targetMember);
        if (isReported) {
            throw new ReportHandler(ErrorStatus.ALREADY_REPORTED);
        }

        // report 엔티티 생성 및 연관관계 매핑
        Report report = Report.builder()
                .target(targetMember)
                .reportType(reportType)
                .reportContent(reportContent)
                .build();
        report.setReporterMember(member);

        reportRepository.save(report);

        return member;
    }
    private void checkBlind (Member member){
        if (member.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }
    }
}
*/

/*
@Service
@Transactional
public class ReportService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;

    @Autowired
    public ReportService(MemberRepository memberRepository, ReportRepository reportRepository, ReportTypeRepository reportTypeRepository) {
        this.memberRepository = memberRepository;
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
    }

    public void createReport(Long reporterId, Long targetId, Long reportTypeId, String reportContent) {
        // 신고자(member)와 신고 대상(targetMember)을 조회
        Member member = memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));
        ReportType reportType = reportTypeRepository.findById(reportTypeId)
                .orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_TYPE_NOT_FOUND));

        // 대상 회원의 탈퇴 여부 검증
        checkBlind(targetMember);

        // 이미 신고한 회원인지 검증
        boolean isReported = reportRepository.existsByReporterAndTarget(member, targetMember);
        if (isReported) {
            throw new ReportHandler(ErrorStatus.ALREADY_REPORTED);
        }

        // report 엔티티 생성 및 연관관계 매핑
        Report report = Report.builder()
                .target(targetMember)
                .reportType(reportType)
                .reportContent(reportContent)
                .build();
        report.setReporter(member);

        reportRepository.save(report);
    }

    private void checkBlind(Member member) {
        if (member.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }
    }
}
*/

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
