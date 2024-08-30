package com.gamegoo.dto.manner;

import lombok.*;

import java.util.List;

public class MannerResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerInsertResponseDTO{
        Long mannerId;
        Long toMemberId;
        List<Long> mannerRatingKeywordList;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerUpdateResponseDTO{
        Long mannerId;
        List<Long> mannerRatingKeywordList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerKeywordResponseDTO{
        Boolean isPositive;
        Boolean isExist;
        List<Long> mannerRatingKeywordList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class badMannerKeywordResponseDTO{
        Boolean isPositive;
        Boolean isExist;
        List<Long> mannerRatingKeywordList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class myMannerResponseDTO{
        Integer mannerLevel;
        List<mannerKeywordDTO> mannerKeywords;
        Double mannerRank;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerByIdResponseDTO{
        Long memberId;
        Integer mannerLevel;
        List<mannerKeywordDTO> mannerKeywords;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class mannerKeywordDTO{
        Boolean isPositive;
        Integer mannerKeywordId;
        Integer count;
    }
}
