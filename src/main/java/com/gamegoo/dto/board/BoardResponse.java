package com.gamegoo.dto.board;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {
    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardInsertResponseDTO{
        Long boardId;
        Long memberId;
        String profileImage;
        String gameName;
        String tag;
        String tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean voice;
        List<Long> gameStyles;
        String contents;

    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardUpdateResponseDTO{
        Long boardId;
        Long memberId;
        String profileImage;
        String gameName;
        String tag;
        String tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean voice;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardListResponseDTO{
        Long boardId;
        Long memberId;
        String profileImage;
        String gameName;
        Integer mannerLevel;
        String tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        List<Long> championList;
        Double winRate;
        LocalDateTime createdAt;
    }
}
