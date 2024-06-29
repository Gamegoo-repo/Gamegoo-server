package com.gamegoo.apiPayload.code.status;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 테스트
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "테스트"),

    // Member 관련 에러
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "MEMBER400", "비밀번호가 불일치합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "해당 사용자를 찾을 수 없습니다."),
    USER_DEACTIVATED(HttpStatus.FORBIDDEN, "MEMBER403", "해당 사용자는 탈퇴한 사용자입니다."),
    MEMBER_CONFLICT(HttpStatus.CONFLICT, "MEMBER409", "이미 있는 사용자입니다."),

    // JWT 관련 에러
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT401", "jwt 토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "JWT400", "유효하지 않은 jwt 토큰입니다."),

    // GameStyle 관련 에러
    GAMESTYLE_NOT_FOUND(HttpStatus.NOT_FOUND, "GAMESTYLE404", "해당 게임 스타일을 찾을 수 없습니다."),

    // Position 관련 에러
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "POSITION404", "해당 Position을 찾을 수 없습니다."),

    // Profile_Image 관련 에러
    PROFILE_IMAGE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PROFILE_IMAGE_400", "profile_image가 30자를 초과했습니다."),

    // Email 인증 관련 에러
    EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL500", "이메일 전송 도중, 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
