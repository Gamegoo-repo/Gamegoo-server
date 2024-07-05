package com.gamegoo.domain.report;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Report")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "report_content", nullable = false, length = 1000)
    private String reportContent;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "report_type_id", nullable = false)
//    private ReportType reportType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Report_ReportType", joinColumns = @JoinColumn(name = "report_id"), inverseJoinColumns = @JoinColumn(name = "report_type_id"))
    private Set<ReportType> reportTypes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private Member target;


//    public void setReporterMember(Member member) {
//        if (this.reporter != null) {
//            this.reporter.getReportList().remove(this); // 기존 reporter의 reportList에서 제거
//        }
//        this.reporter = member;
//        member.getReportList().add(this);  // 새로운 reporter의 reportList에 추가
//
//    }

}
