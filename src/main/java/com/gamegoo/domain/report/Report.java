package com.gamegoo.domain.report;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private Member target;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportTypeMapping> reportTypeMappingList = new ArrayList<>();


//    public void setReporterMember(Member member) {
//        if (this.reporter != null) {
//            this.reporter.getReportList().remove(this); // 기존 reporter의 reportList에서 제거
//        }
//        this.reporter = member;
//        member.getReportList().add(this);  // 새로운 reporter의 reportList에 추가
//
//    }

}
