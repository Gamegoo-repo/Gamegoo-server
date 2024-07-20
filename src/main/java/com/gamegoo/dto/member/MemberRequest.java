package com.gamegoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

public class MemberRequest {
    @Getter
    public static class EmailCodeRequestDTO {
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        private String email;
        @NotBlank
        private String code;
    }

    @Getter
    public static class EmailRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        private String email;
    }

    @Getter
    public static class GameStyleRequestDTO {
        private List<Long> gameStyleIdList;
    }

    @Getter
    @AllArgsConstructor
    public static class JoinRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        private String email;
        @NotBlank
        private String password;

    }

    @Getter
    @AllArgsConstructor
    public static class PasswordRequestDTO {
        @NotBlank
        private String password;
    }

    @Getter
    @Min(0)
    @Max(5)
    public static class PositionRequestDTO {
        @NotBlank
        int mainP;
        @NotBlank
        int subP;
    }

    @Getter
    @AllArgsConstructor
    public static class ProfileImageRequestDTO {
        @NonNull
        @NotBlank
        String profileImage;
    }

    @Getter
    @AllArgsConstructor
    public static class RefreshTokenRequestDTO {
        @NotBlank
        String refreshToken;
    }

    @Getter
    @AllArgsConstructor
    public static class verifyRiotRequestDTO {
        @NotBlank
        String email;
        @NotBlank
        String gameName;
        @NotBlank
        String tag;
    }
}
