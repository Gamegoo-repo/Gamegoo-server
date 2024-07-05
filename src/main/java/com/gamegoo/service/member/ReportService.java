package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BlockHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.ReportHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.report.Report;
import com.gamegoo.domain.report.ReportType;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.repository.member.ReportRepository;
import com.gamegoo.repository.member.ReportTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
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
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository, ReportTypeRepository reportTypeRepository, MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
        this.memberRepository = memberRepository;
    }

    public void createReport(Long reporterId, Long targetId, List<Long> reportTypeIds, String reportContent) {
        // 신고자(reporter)와 신고 대상(targetMember), 신고 타입(reportType)을 조회
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));
//        ReportType reportType = reportTypeRepository.findById(reportTypeId)
//                .orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_TYPE_NOT_FOUND));

        // 대상 회원의 탈퇴 여부 검증
        checkBlind(targetMember);

        // 이미 신고한 회원인지 검증
        boolean isReported = reportRepository.existsByReporterAndTarget(reporter, targetMember);
        if (isReported) {
            throw new ReportHandler(ErrorStatus.ALREADY_REPORTED);
        }

        Set<ReportType> reportTypes = reportTypeIds.stream()
                .map(id -> reportTypeRepository.findById(id)
                        .orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_TYPE_NOT_FOUND)))
                .collect(Collectors.toSet());

        Report report = Report.builder()
                .reporter(reporter)
                .target(targetMember)
                .reportTypes(reportTypes)
                .reportContent(reportContent)
                .build();

        reportRepository.save(report);
    }

    public static boolean checkBlind(Member member) {
        if (member.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }
        return false;
    }
}
