package com.gamegoo.controller.chat;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.ChatConverter;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1")
@Tag(name = "Chat", description = "Chat 관련 API")
public class ChatController {

    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;

    @Operation(summary = "채팅방 uuid 조회 API", description = "회원이 속한 채팅방의 uuid를 조회하는 API 입니다.")
    @GetMapping("/member/chatroom/uuid")
    public ApiResponse<List<String>> getChatroomUuid() {
        Long memberId = JWTUtil.getCurrentUserId();
        List<String> chatroomUuids = chatQueryService.getChatroomUuids(memberId);
        return ApiResponse.onSuccess(chatroomUuids);
    }

    @Operation(summary = "채팅방 목록 조회 API", description = "회원이 속한 채팅방 목록을 조회하는 API 입니다.")
    @Parameter(name = "cursor", description = "페이징을 위한 커서, 이전 페이지의 마지막 채팅방의 lastMsgTimestamp입니다. 13자리 timestamp integer를 보내주세요.")
    @GetMapping("/member/chatroom")
    public ApiResponse<ChatResponse.ChatroomViewListDTO> getChatroom(
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        return ApiResponse.onSuccess(chatQueryService.getChatroomList(memberId, cursor));
    }

    @Operation(summary = "특정 회원과 채팅방 시작 API", description = "특정 대상 회원과의 채팅방을 시작하는 API 입니다.\n\n" +
        "대상 회원과의 채팅방이 이미 존재하는 경우, 채팅방 uuid, 상대 회원 정보와 채팅 메시지 내역 등을 리턴합니다.\n\n" +
        "대상 회원과의 채팅방이 존재하지 않는 경우, 채팅방을 새로 생성해 해당 채팅방의 uuid, 상대 회원 정보 등을 리턴합니다.")
    @Parameter(name = "memberId", description = "채팅방을 시작할 대상 회원의 id 입니다.")
    @GetMapping("/chat/start/member/{memberId}")
    public ApiResponse<ChatResponse.ChatroomEnterDTO> startChatroomByMemberId(
        @PathVariable(name = "memberId") Long targetMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        return ApiResponse.onSuccess(
            chatCommandService.startChatroomByMemberId(memberId, targetMemberId));
    }

    @Operation(summary = "특정 글을 보고 채팅방 시작 API", description =
        "특정 글에서 말 걸어보기 버튼을 통해 채팅방을 시작하는 API 입니다.\n\n" +
            "대상 회원과의 채팅방이 이미 존재하는 경우, 채팅방 uuid, 상대 회원 정보와 채팅 메시지 내역 등을 리턴합니다.\n\n" +
            "대상 회원과의 채팅방이 존재하지 않는 경우, 채팅방을 새로 생성해 해당 채팅방의 uuid, 상대 회원 정보 등을 리턴합니다.")
    @Parameter(name = "boardId", description = "말 걸어보기 버튼을 누른 게시글의 id 입니다.")
    @GetMapping("/chat/start/board/{boardId}")
    public ApiResponse<ChatResponse.ChatroomEnterDTO> startChatroomByBoardId(
        @PathVariable(name = "boardId") Long boardId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        return ApiResponse.onSuccess(
            chatCommandService.startChatroomByBoardId(memberId, boardId));
    }

    @Operation(summary = "채팅방 입장 API", description = "특정 채팅방에 입장하는 API 입니다. 채팅 상대의 id, 프로필 이미지, 닉네임 및 해당 채팅방의 안읽은 메시지 및 최근 메시지 목록을 리턴합니다.")
    @GetMapping("/chat/{chatroomUuid}/enter")
    public ApiResponse<ChatResponse.ChatroomEnterDTO> enterChatroom(
        @PathVariable(name = "chatroomUuid") String chatroomUuid
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        return ApiResponse.onSuccess(chatCommandService.enterChatroom(chatroomUuid, memberId));
    }

    @Operation(summary = "채팅 메시지 등록 API", description = "새로운 채팅 메시지를 등록하는 API 입니다.")
    @PostMapping("/chat/{chatroomUuid}")
    public ApiResponse<ChatResponse.ChatCreateResultDTO> addChat(
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestBody @Valid ChatRequest.ChatCreateRequest request
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        Chat chat = chatCommandService.addChat(request, chatroomUuid, memberId);

        return ApiResponse.onSuccess(ChatConverter.toChatCreateResultDTO(chat));
    }

    @Operation(summary = "채팅 내역 조회 API", description = "특정 채팅방의 메시지 내역을 조회하는 API 입니다.\n\n" +
        "cursor 파라미터를 보내면, 해당 timestamp 이전에 전송된 메시지 최대 20개를 조회합니다.\n\n" +
        "cursor 파라미터를 보내지 않으면, 해당 채팅방의 가장 최근 메시지 내역을 조회합니다.")
    @GetMapping("/chat/{chatroomUuid}/messages")
    @Parameter(name = "cursor", description = "페이징을 위한 커서, 13자리 timestamp integer를 보내주세요. (UTC 기준)")
    public ApiResponse<ChatResponse.ChatMessageListDTO> getChatMessages(
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        Slice<Chat> chatMessages = chatQueryService.getChatMessagesByCursor(chatroomUuid, memberId,
            cursor);

        return ApiResponse.onSuccess(ChatConverter.toChatMessageListDTO(chatMessages));
    }

    @Operation(summary = "채팅 메시지 읽음 처리 API", description = "특정 채팅방의 메시지를 읽음 처리하는 API 입니다.")
    @PatchMapping("/chat/{chatroomUuid}/read")
    @Parameter(name = "timestamp", description = "특정 메시지를 읽음 처리하는 경우, 그 메시지의 timestamp를 함께 보내주세요.")
    public ApiResponse<String> readChatMessage(
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestParam(name = "timestamp", required = false) Long timestamp
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        chatCommandService.readChatMessages(chatroomUuid, timestamp, memberId);
        return ApiResponse.onSuccess("채팅 메시지 읽음 처리 성공");
    }

    @Operation(summary = "채팅방 나가기 API", description = "채팅방 나가기 API 입니다.")
    @PatchMapping("/chat/{chatroomUuid}/exit")
    public ApiResponse<Object> exitChatroom(
        @PathVariable(name = "chatroomUuid") String chatroomUuid
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        chatCommandService.exitChatroom(chatroomUuid, memberId);
        return ApiResponse.onSuccess("채팅방 나가기 성공");
    }

    @Operation(summary = "안읽은 채팅방 uuid 목록 조회 API", description = "안읽은 메시지가 속한 채팅방의 uuid 목록을 조회하는 API 입니다.")
    @GetMapping("/chat/unread")
    public ApiResponse<List<String>> getUnreadChatroomUuid() {
        Long memberId = JWTUtil.getCurrentUserId();
        List<String> chatroomUuids = chatQueryService.getUnreadChatroomUuids(memberId);
        return ApiResponse.onSuccess(chatroomUuids);
    }

    @Operation(summary = "매칭을 통한 채팅방 시작 메소드 테스트용 API", description =
        "매칭을 통한 채팅방 시작 메소드를 테스트하기 위한 API 입니다.\n\n" +
            "대상 회원과의 채팅방이 이미 존재하는 경우, 해당 채팅방 uuid를 리턴합니다.\n\n" +
            "대상 회원과의 채팅방이 존재하지 않는 경우, 채팅방을 새로 생성해 해당 채팅방의 uuid를 리턴합니다.")
    @Parameters({
        @Parameter(name = "memberId1", description = "매칭 시켜줄 회원 id 입니다."),
        @Parameter(name = "memberId2", description = "매칭 시켜줄 회원 id 입니다.")
    })
    @GetMapping("/chat/start/matching/{memberId1}/{memberId2}")
    public ApiResponse<String> startChatroomByMatching(
        @PathVariable(name = "memberId1") Long memberId1,
        @PathVariable(name = "memberId2") Long memberId2
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        return ApiResponse.onSuccess(
            chatCommandService.startChatroomByMatching(memberId1, memberId2));
    }

}
