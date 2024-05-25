package com.gamegoo.domain.report;

import lombok.*;
import org.w3c.dom.Text;

import javax.persistence.*;

@Entity
@Table(name = "Report")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "report_content", nullable = false)
    private Text reportContent;
}
