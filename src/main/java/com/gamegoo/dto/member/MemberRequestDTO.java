package com.gamegoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MemberRequestDTO {
    @Setter
    @Getter
    public static class EmailCodeRequestDTO {
        @NotNull
        private String email;
        @NotNull
        private String code;
    }

    @Setter
    @Getter
    public static class EmailRequestDTO {
        @NotNull
        private String email;
    }

    @Getter
    @Setter
    public static class GameStyleRequestDTO {
        private List<Long> gameStyleIdList;
    }

    @Getter
    public static class JoinRequestDTO {
        @NotNull
        private String email;
        @NotNull
        private String password;

    }

    @Getter
    @Setter
    public static class PasswordRequestDTO {
        @NonNull
        private String password;
    }

    @Getter
    @Setter
    @Min(0)
    @Max(5)
    public static class PositionRequestDTO {
        @NonNull
        int mainP;
        @NotNull
        int subP;
    }

    @Getter
    @Setter
    public static class ProfileImageRequestDTO {
        @NotNull
        String profile_image;
    }

    @Getter
    @Setter
    public static class RefreshTokenRequestDTO {
        String refresh_token;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RefreshTokenResponseDTO {
        String access_token;
        String refresh_token;
    }
}
