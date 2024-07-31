package com.gamegoo.dto.manner;

import lombok.*;

import java.util.List;

public class MannerResponse {
    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerInsertResponseDTO{
        Long mannerId;
        Long toMemberId;
        List<Long> mannerRatingKeywordList;
    }
}
