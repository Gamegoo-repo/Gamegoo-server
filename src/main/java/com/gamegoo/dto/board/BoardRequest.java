package com.gamegoo.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

public class BoardRequest {
    @Getter
    @Setter
    public static class boardInsertDTO {
        Integer boardProfileImage;
        @NotNull
        Integer gameMode;

        @NotNull
        Integer mainPosition;

        @NotNull
        Integer subPosition;

        @NotNull
        Integer wantPosition;

        @Schema(description = "마이크 사용 여부", defaultValue = "false")
        Boolean mike = false;
        List<Long> gameStyles;
        String contents;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class boardUpdateDTO {
        Integer boardProfileImage;
        Integer gameMode;

        Integer mainPosition;

        Integer subPosition;

        Integer wantPosition;
        Boolean mike;
        List<Long> gameStyles;
        String contents;
    }
}
