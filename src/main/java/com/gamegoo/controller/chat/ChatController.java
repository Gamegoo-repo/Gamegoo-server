package com.gamegoo.controller.chat;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.ChatConverter;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "채팅방 생성 API", description = "채팅방을 생성하는 API 입니다.")
    @PostMapping("/chatroom/create/test")
    public ApiResponse<ChatResponse.ChatroomCreateResultDTO> createChatroom(
        @RequestBody @Valid ChatRequest.ChatroomCreateRequest request
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        Chatroom chatroom = chatCommandService.createChatroom(request, memberId);
        return ApiResponse.onSuccess(
            ChatConverter.toChatroomCreateResultDTO(chatroom, request.getTargetMemberId()));
    }

    @Operation(summary = "채팅방 생성 by Matching API", description = "매칭을 통한 채팅방을 생성하는 API 입니다.")
    @PostMapping("/chatroom/create/matched")
    public ApiResponse<ChatResponse.ChatroomCreateResultDTO> createChatroomByMatching(
        @RequestBody @Valid ChatRequest.ChatroomCreateByMatchRequest request
    ) {
        Chatroom chatroomByMatch = chatCommandService.createChatroomByMatch(request);
        return ApiResponse.onSuccess(
            ChatConverter.toChatroomCreateResultDTO(chatroomByMatch, null));
        
    }

    @Operation(summary = "채팅방 목록 조회 API", description = "회원이 속한 채팅방 목록을 조회하는 API 입니다.")
    @GetMapping("/member/chatroom")
    public ApiResponse<List<ChatResponse.ChatroomViewDTO>> getChatroom() {
        Long memberId = JWTUtil.getCurrentUserId();

        return ApiResponse.onSuccess(chatQueryService.getChatroomList(memberId));
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
        @RequestBody ChatRequest.ChatCreateRequest request
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
    public ApiResponse<Object> getChatMessages(
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        Slice<Chat> chatMessages = chatQueryService.getChatMessagesByCursor(chatroomUuid, memberId,
            cursor);

        return ApiResponse.onSuccess(ChatConverter.toChatMessageListDTO(chatMessages));
    }

    @Operation(summary = "채팅 메시지 읽음 처리 API", description = "특정 채팅방의 메시지를 읽음 처리하는 API 입니다.")
    @GetMapping("/chatroom/{chatroomUuid}/read")
    @Parameter(name = "timestamp", description = "특정 메시지를 읽음 처리하는 경우, 그 메시지의 timestamp를 함께 보내주세요.")
    public ApiResponse<String> readChatMessage(
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestParam(name = "timestamp", required = false) Long timestamp
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        chatCommandService.readChatMessages(chatroomUuid, timestamp, memberId);
        return ApiResponse.onSuccess("채팅 메시지 읽음 처리 성공");
    }
}
