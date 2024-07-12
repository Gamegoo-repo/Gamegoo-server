package com.gamegoo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
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

        Boolean voice=false;

        String contents;

    }
}
