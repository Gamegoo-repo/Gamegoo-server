package com.gamegoo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReportResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class reportInsertResultDTO{
        Long reportId;
        Long targetId;
        List<Long> reportTypeIdList;
        String contents;
    }
}
