package com.gamegoo.dto.board;

import lombok.*;

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
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean voice;
        List<Long> gameStyles;
        String contents;
    }
}
