package com.gamegoo.dto.board;

import com.gamegoo.domain.member.Tier;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {
    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardInsertResponseDTO {
        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean mike;
        List<Long> gameStyles;
        String contents;

    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardUpdateResponseDTO {
        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean mike;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardListResponseDTO {
        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        Integer mannerLevel;
        Tier tier;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        List<Long> championList;
        Double winRate;
        LocalDateTime createdAt;
        Boolean mike;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardByIdResponseDTO {
        Long boardId;
        Long memberId;
        LocalDateTime createdAt;
        Integer profileImage;
        String gameName;
        String tag;
        Integer mannerLevel;
        Tier tier;
        Boolean mike;
        List<Long> championList;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Double winRate;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class myBoardListResponseDTO {
        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        String contents;
        LocalDateTime createdAt;
    }
}
