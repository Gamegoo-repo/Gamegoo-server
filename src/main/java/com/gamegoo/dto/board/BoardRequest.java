package com.gamegoo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class BoardRequest {
    @Getter
    @Setter
    public static class boardInsertDTO{
        @NotNull
        Integer gameMode;

        @NotNull
        Integer mainPosition;

        @NotNull
        Integer subPosition;

        @NotNull
        Integer wantPosition;

        @Schema(description = "마이크 사용 여부", defaultValue = "false")
        Boolean voice=false;
        List<Long> gameStyles;
        String contents;

    }
}
