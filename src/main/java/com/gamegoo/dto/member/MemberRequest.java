package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

public class MemberRequest {
    @Getter
    @NoArgsConstructor
    public static class EmailCodeRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        String email;
        String code;
    }

    @Getter
    @NoArgsConstructor
    public static class EmailRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        String email;
    }

    @Getter
    public static class GameStyleRequestDTO {
        @NotBlank(message = "gameStyleList은 비워둘 수 없습니다.")
        List<Long> gameStyleIdList;
    }

    @Getter
    @NoArgsConstructor
    public static class JoinRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        String email;
        @NotBlank
        String password;

    }

    @Getter
    public static class PasswordRequestDTO {
        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password;
    }

    @Getter
    public static class PositionRequestDTO {
        @Min(0)
        @Max(5)
        int mainP;
        @Min(0)
        @Max(5)
        int subP;
    }

    @Getter
    public static class ProfileImageRequestDTO {
        String profileImage;
    }

    @Getter
    public static class RefreshTokenRequestDTO {
        @NotBlank
        String refreshToken;
    }

    @Getter
    public static class verifyRiotRequestDTO {
        @NotBlank
        String email;
        @NotBlank
        String gameName;
        @NotBlank
        String tag;
    }
}
