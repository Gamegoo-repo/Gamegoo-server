package com.gamegoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MemberRequest {
    @Getter
    @NotBlank
    public static class EmailCodeRequestDTO {
        @NotNull
        private String email;
        @NotNull
        private String code;
    }

    @Getter
    @NotBlank
    public static class EmailRequestDTO {
        @NotNull
        private String email;
    }

    @Getter
    public static class GameStyleRequestDTO {
        private List<Long> gameStyleIdList;
    }

    @Getter
    @AllArgsConstructor
    @NotBlank
    public static class JoinRequestDTO {
        private String email;
        private String password;

    }

    @Getter
    @AllArgsConstructor
    @NotBlank
    public static class PasswordRequestDTO {
        private String password;
    }

    @Getter
    @NotBlank
    @Min(0)
    @Max(5)
    public static class PositionRequestDTO {
        int mainP;
        int subP;
    }

    @Getter
    @AllArgsConstructor
    public static class ProfileImageRequestDTO {
        @NonNull
        String profileImage;
    }

    @Getter
    @AllArgsConstructor
    @NotBlank
    public static class RefreshTokenRequestDTO {
        String refreshToken;
    }

    @Getter
    @AllArgsConstructor
    @NotBlank
    public static class verifyRiotRequestDTO {
        String email;
        String gameName;
        String tag;
    }
}
