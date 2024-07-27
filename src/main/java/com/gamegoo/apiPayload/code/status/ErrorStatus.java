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

    // 페이징 관련 에러
    PAGE_INVALID(HttpStatus.BAD_REQUEST, "PAGE401", "페이지 값은 1 이상이어야 합니다."),

    // Member 관련 에러
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "MEMBER400", "비밀번호가 불일치합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "해당 사용자를 찾을 수 없습니다."),
    USER_DEACTIVATED(HttpStatus.FORBIDDEN, "MEMBER403", "해당 사용자는 탈퇴한 사용자입니다."),
    MEMBER_CONFLICT(HttpStatus.CONFLICT, "MEMBER409", "이미 있는 사용자입니다."),

    // JWT 관련 에러
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT401", "jwt 토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "JWT400", "유효하지 않은 jwt 토큰입니다."),
    TOKEN_NULL(HttpStatus.NOT_FOUND, "JWT404", "JWT 토큰이 없습니다."),

    // GameStyle 관련 에러
    GAMESTYLE_NOT_FOUND(HttpStatus.NOT_FOUND, "GAMESTYLE404", "해당 게임 스타일을 찾을 수 없습니다."),

    // Position 관련 에러
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "POSITION404", "해당 Position을 찾을 수 없습니다."),

    // Profile_Image 관련 에러
    PROFILE_IMAGE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PROFILE_IMAGE_400", "profile_image가 30자를 초과했습니다."),

    // Email 인증 관련 에러
    EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL500", "이메일 전송 도중, 에러가 발생했습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL404", "해당 이메일을 찾을 수 없습니다."),
    EMAIL_INVALID_CODE(HttpStatus.BAD_REQUEST, "EMAIL400", "인증 코드가 불일치합니다."),
    EMAIL_INVALID_TIME(HttpStatus.BAD_REQUEST, "EMAIL400", "이메일 인증 시간이 3분 초과했습니다."),

    // Riot 관련 에러
    RIOT_NOT_FOUND(HttpStatus.NOT_FOUND, "RIOT404", "해당 Riot 계정이 존재하지 않습니다."),
    RIOT_MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "RIOTMATCH404", "해당 Riot 계정의 매칭을 불러오는 도중 에러가 발생했습니다."),
    CHAMPION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAMPION404", "해당 챔피언이 존재하지 않습니다."),
    RIOT_MEMBER_CONFLICT(HttpStatus.CONFLICT, "RIOT409", "해당 이메일 계정은 이미 다른 RIOT 계정과 연동되었습니다."),
    RIOT_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "RIOT409", "해당 RIOT 계정은 이미 다른 이메일과 연동되어있습니다."),
    RIOT_INSUFFICIENT_MATCHES(HttpStatus.NOT_FOUND, "RIOT404", "해당 RIOT 계정은 최근 100판 이내에 솔로랭크, 자유랭크, 일반게임, 칼바람을 플레이한 적이 없기 때문에 선호하는 챔피언 3명을 정할 수 없습니다."),
    RIOT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RIOT500", "RIOT API 연동 중 에러가 발생했습니다."),

    // 차단 관련 에러
    TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK401", "차단 대상 회원을 찾을 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "BLOCK402", "이미 차단한 회원입니다."),
    TARGET_MEMBER_NOT_BLOCKED(HttpStatus.BAD_REQUEST, "BLOCK403", "차단 목록에 존재하지 않는 회원입니다."),

    // 신고 관련 에러
    REPORT_TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT401", "신고 대상 회원을 찾을 수 없습니다."),
    MEMBER_AND_TARGET_MEMBER_SAME(HttpStatus.BAD_REQUEST, "REPORT402", "회원과 신고 대상 회원이 같습니다.");

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
