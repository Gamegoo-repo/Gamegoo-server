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
    CURSOR_INVALID(HttpStatus.BAD_REQUEST, "PAGE402", "커서 값은 1 이상이어야 합니다."),

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

    // Email 인증 관련 에러
    EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL500", "이메일 전송 도중, 에러가 발생했습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL404", "해당 이메일을 찾을 수 없습니다."),
    EMAIL_INVALID_CODE(HttpStatus.BAD_REQUEST, "EMAIL400", "인증 코드가 불일치합니다."),
    EMAIL_INVALID_TIME(HttpStatus.BAD_REQUEST, "EMAIL400", "이메일 인증 시간이 3분 초과했습니다."),

    // 매칭 관련 에러
    MATCHING_STATUS_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MATCH400",
        "status는 SUCCESS, FAIL 둘 중 하나로만 변경이 가능합니다."),
    MATHCING_TYPE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MATCH400",
        "matchingType은 BASIC, PRECISE 둘 중 하나여야합니다."),
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH404", "해당 사용자의 매칭 정보가 없습니다."),


    // Riot 관련 에러
    RIOT_NOT_FOUND(HttpStatus.NOT_FOUND, "RIOT404", "해당 Riot 계정이 존재하지 않습니다."),
    RIOT_MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "RIOTMATCH404",
        "해당 Riot 계정의 매칭을 불러오는 도중 에러가 발생했습니다. 최근 100판 이내 이벤트 매칭 제외, 일반 매칭(일반게임,랭크게임,칼바람)을 많이 한 계정으로 다시 시도하세요."),
    RIOT_PREFER_CHAMPION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RIOTCHAMPION500",
        "선호 챔피언을 연동하는 도중 에러가 발생했습니다"),
    CHAMPION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAMPION404", "해당 챔피언이 존재하지 않습니다."),
    RIOT_MEMBER_CONFLICT(HttpStatus.CONFLICT, "RIOT409", "해당 이메일 계정은 이미 다른 RIOT 계정과 연동되었습니다."),
    RIOT_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "RIOT409", "해당 RIOT 계정은 이미 다른 이메일과 연동되어있습니다."),
    RIOT_INSUFFICIENT_MATCHES(HttpStatus.NOT_FOUND, "RIOT404",
        "해당 RIOT 계정은 최근 100판 이내에 솔로랭크, 자유랭크, 일반게임, 칼바람을 플레이한 적이 없기 때문에 선호하는 챔피언 3명을 정할 수 없습니다."),
    RIOT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RIOT500", "RIOT API 연동 중 에러가 발생했습니다."),

    // 차단 관련 에러
    TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK401", "차단 대상 회원을 찾을 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "BLOCK402", "이미 차단한 회원입니다."),
    TARGET_MEMBER_NOT_BLOCKED(HttpStatus.BAD_REQUEST, "BLOCK403", "차단 목록에 존재하지 않는 회원입니다."),
    BLOCK_MEMBER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "BLOCK404", "잘못된 친구 차단 요청입니다."),

    // 신고 관련 에러
    REPORT_TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT401", "신고 대상 회원을 찾을 수 없습니다."),
    MEMBER_AND_TARGET_MEMBER_SAME(HttpStatus.BAD_REQUEST, "REPORT402", "회원과 신고 대상 회원이 같습니다."),

    // 게시판 글 작성 관련 에러
    BOARD_GAME_STYLE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "BOARD400",
        "게임 스타일 선택 개수(최대 3개)를 초과했습니다."),
    GAME_MODE_INVALID(HttpStatus.BAD_REQUEST, "BOARD401", "게임모드 값은 1~4만 가능합니다."),
    MAIN_POSITION_INVALID(HttpStatus.BAD_REQUEST, "BOARD401", "주포지션 값은 0~5만 가능합니다."),
    SUB_POSITION_INVALID(HttpStatus.BAD_REQUEST, "BOARD401", "부포지션 값은 0~5만 가능합니다."),
    WANT_POSITION_INVALID(HttpStatus.BAD_REQUEST, "BOARD401", "상대포지션 값은 0~5만 가능합니다."),

    // 게시판 글 수정, 조회 관련 에러
    BOARD_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "BOARD401", "글 작성자만 수정 가능합니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD404", "게시판에서 해당 글을 찾을 수 없습니다."),

    // 게시판 글 삭제 관련 에러
    BOARD_DELETE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "BOARD401", "글 작성자만 삭제 가능합니다."),

    // 매너평가 관련 에러
    MANNER_TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MANNER401", "매너 평가 대상 회원을 찾을 수 없습니다."),
    BAD_MANNER_TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MANNER401",
        "비매너 평가 대상 회원을 찾을 수 없습니다."),
    MANNER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MANNER401", "매너평가 작성자만 수정 가능합니다."),
    MANNER_KEYWORD_TYPE_INVALID(HttpStatus.BAD_REQUEST, "MANNER401", "매너 키워드 유형은 1~6만 가능합니다."),
    BAD_MANNER_KEYWORD_TYPE_INVALID(HttpStatus.BAD_REQUEST, "MANNER401",
        "비매너 키워드 유형은 7~12만 가능합니다."),
    MANNER_KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "MANNER404", "해당 매너 키워드를 찾을 수 없습니다."),
    MANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "MANNER404", "해당 매너평가를 찾을 수 없습니다."),
    BAD_MANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "MANNER404", "해당 비매너평가를 찾을 수 없습니다."),
    MANNER_CONFLICT(HttpStatus.CONFLICT, "MANNER409", "매너 평가는 최초 1회만 가능합니다."),
    BAD_MANNER_CONFLICT(HttpStatus.CONFLICT, "MANNER409", "비매너 평가는 최초 1회만 가능합니다."),
    MANNER_INSERT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MANNER410", "잘못된 매너평가 등록 요청입니다."),

    // 채팅 관련 에러
    CHAT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT401", "채팅 대상 회원을 찾을 수 없습니다."),
    CHATROOM_NOT_EXIST(HttpStatus.NOT_FOUND, "CHAT402", "채팅방을 찾을 수 없습니다."),
    CHATROOM_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "CHAT403", "접근할 수 없는 채팅방 입니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404", "해당 메시지를 찾을 수 없습니다"),
    CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED(HttpStatus.FORBIDDEN, "CHAT405",
        "채팅 상대 회원을 차단한 상태입니다. 채팅 시작이 불가능합니다."),
    BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED(HttpStatus.FORBIDDEN, "CHAT406",
        "채팅 상대 회원이 나를 차단했습니다. 채팅 시작이 불가능합니다."),
    CHAT_TARGET_IS_BLOCKED_SEND_CHAT_FAILED(HttpStatus.FORBIDDEN, "CHAT407",
        "채팅 상대 회원을 차단한 상태입니다. 채팅 메시지 전송이 불가능합니다."),
    BLOCKED_BY_CHAT_TARGET_SEND_CHAT_FAILED(HttpStatus.FORBIDDEN, "CHAT408",
        "채팅 상대 회원이 나를 차단했습니다. 채팅 메시지 전송이 불가능합니다."),
    CHAT_TARGET_MEMBER_ID_INVALID(HttpStatus.BAD_REQUEST, "CHAT409", "채팅방 시작 대상 회원 id 값이 잘못되었습니다."),

    // 친구 관련 에러
    FRIEND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND401", "잘못된 친구 요청입니다."),
    FRIEND_TARGET_IS_BLOCKED(HttpStatus.BAD_REQUEST, "FRIEND402",
        "내가 차단한 회원입니다. 친구 요청을 보낼 수 없습니다."),
    BLOCKED_BY_FRIEND_TARGET(HttpStatus.BAD_REQUEST, "FRIEND403",
        "나를 차단한 회원입니다. 친구 요청을 보낼 수 없습니다."),
    MY_PENDING_FRIEND_REQUEST_EXIST(HttpStatus.BAD_REQUEST, "FRIEND404",
        "해당 회원에게 보낸 수락 대기 중인 친구 요청이 존재합니다. 친구 요청을 보낼 수 없습니다."),
    TARGET_PENDING_FRIEND_REQUEST_EXIST(HttpStatus.BAD_REQUEST, "FRIEND405",
        "해당 회원이 나에게 보낸 친구 요청이 수락 대기 중 입니다. 해당 요청을 수락 해주세요."),
    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "FRIEND406",
        "두 회원은 이미 친구 관계 입니다. 친구 요청을 보낼 수 없습니다."),
    PENDING_FRIEND_REQUEST_NOT_EXIST(HttpStatus.NOT_FOUND, "FRIEND407",
        "취소/수락/거절할 친구 요청이 존재하지 않습니다."),
    MEMBERS_NOT_FRIEND(HttpStatus.BAD_REQUEST, "FRIEND408", "두 회원은 친구 관계가 아닙니다."),
    ALREADY_STAR_FRIEND(HttpStatus.BAD_REQUEST, "FRIEND409", "이미 즐겨찾기 되어 있는 친구입니다."),
    NOT_STAR_FRIEND(HttpStatus.BAD_REQUEST, "FRIEND410", "즐겨찾기 되어 있는 친구가 아닙니다."),
    FRIEND_SEARCH_QUERY_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FRIEND411",
        "친구 검색 쿼리는 100자 이하여야 합니다."),

    // 알림 관련 에러
    NOTIFICATION_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI401", "해당 알림 타입 데이터를 찾을 수 없습니다."),
    NOTIFICATION_METHOD_BAD_REQUEST(HttpStatus.BAD_REQUEST, "NOTI402", "알림 생성 메소드 호출이 잘못되었습니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTI403",
        "잘못된 알림 조회 타입입니다. general과 friend 중 하나를 입력하세요."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI404", "해당 알림 내역을 찾을 수 없습니다."),

    // SOCKET 서버 API 호출 에러
    SOCKET_API_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SOCKET501",
        "socket서버 api 요청에 실패했습니다.");

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
