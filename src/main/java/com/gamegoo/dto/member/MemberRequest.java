package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

public class MemberRequest {

    @Getter
    @NoArgsConstructor
    public static class EmailCodeRequestDTO {

        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        String email;
        @NotBlank(message = "인증코드(code)는 비워둘 수 없습니다.")
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
        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password;
        @NotBlank(message = "gameName 값은 비워둘 수 없습니다.")
        String gameName;
        @NotBlank(message = "tag 값은 비워둘 수 없습니다.")
        String tag;
        @NotNull(message = "isAgree 값은 비워둘 수 없습니다. true/false 둘 중 하나를 반드시 포함해야합니다.")
        Boolean isAgree;

    }

    @Getter
    public static class PasswordRequestJWTDTO {
        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password;

    }

    @Getter
    public static class PasswordRequestDTO {
        @Email(message = "Email 형식이 올바르지 않습니다.")
        @NotBlank(message = "Email은 비워둘 수 없습니다.")
        String email;
        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password;

    }


    @Getter
    public static class PositionRequestDTO {
        @Min(value = 0, message = "메인 포지션의 값은 0이상이어야 합니다.")
        @Max(value = 5, message = "메인 포지션의 값은 5이하이어야 합니다.")
        int mainP;
        @Min(value = 0, message = "서브 포지션의 값은 0이상이어야 합니다.")
        @Max(value = 5, message = "서브 포지션의 값은 5이하이어야합니다.")
        int subP;
    }

    @Getter
    public static class ProfileImageRequestDTO {
        @Min(value = 1, message = "프로필 이미지의 값은 1이상이어야 합니다.")
        @Max(value = 8, message = "프로필 이미지의 값은 8이하이어야 합니다.")
        @NotNull(message = "profileImage 값은 비워둘 수 없습니다.")
        Integer profileImage;
    }

    @Getter
    public static class MikeRequestDTO {
        @NotNull(message = "isMike 값은 비워둘 수 없습니다.")
        Boolean isMike;
    }

    @Getter
    public static class RefreshTokenRequestDTO {
        @NotBlank(message = "refreshToken 값은 비워둘 수 없습니다.")
        String refreshToken;
    }

    @Getter
    public static class verifyRiotRequestDTO {
        @NotBlank(message = "gameName 값은 비워둘 수 없습니다.")
        String gameName;
        @NotBlank(message = "tag 값은 비워둘 수 없습니다.")
        String tag;
    }
}
