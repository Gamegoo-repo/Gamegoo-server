package com.gamegoo.dto.manner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class mannerUpdateDTO {
        @NotEmpty
        List<Long> mannerRatingKeywordList;
    }
}
