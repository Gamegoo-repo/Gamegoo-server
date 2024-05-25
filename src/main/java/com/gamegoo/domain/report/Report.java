package com.gamegoo.domain.report;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;
import org.w3c.dom.Text;

import javax.persistence.*;

@Entity
@Table(name = "Report")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "report_content", nullable = false)
    private Text reportContent;
}
