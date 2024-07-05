package com.gamegoo.dto.report;

import lombok.Getter;

public class ReportRequest {

    @Getter
    public class ReportRequestDTO {
        private Long reporterId;

        private Long targetId;

        private Long reportTypeId;

        private String reportContent;
    }
}
