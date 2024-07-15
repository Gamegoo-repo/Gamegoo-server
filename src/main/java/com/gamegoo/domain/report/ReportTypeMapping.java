package com.gamegoo.domain.report;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "ReportTypeMapping")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportTypeMapping extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_type_mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id", nullable = false)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    // 연관관계 메소드
    public void setReport(Report report) {
        if (this.report != null) {
            this.report.getReportTypeMappingList().remove(this);
        }
        this.report = report;
        this.report.getReportTypeMappingList().add(this);
    }

}
