package com.gamegoo.dto.report;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class ReportRequest {

    @Getter
    public static class reportInsertDTO {
        @NotNull
        Long targetMemberId;

        @NotEmpty
        List<Long> reportTypeIdList;

        String contents;
    }
}
