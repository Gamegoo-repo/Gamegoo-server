package com.gamegoo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BoardResponse {
    @Getter
    @Builder
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
        String contents;

    }
}
