package com.gamegoo.dto.manner;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MannerRequest {

    @Getter
    public static class mannerInsertDTO {
        @NotNull
        Long toMemberId;

        @NotEmpty
        List<Long> mannerRatingKeywordList;

    }
}
