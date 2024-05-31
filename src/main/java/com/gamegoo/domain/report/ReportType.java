package com.gamegoo.domain.report;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "ReportType")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportType extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_type_id")
    private Long id;
    @Column(name = "report_type_content", nullable = false)
    private String reportTypeContent;

}
