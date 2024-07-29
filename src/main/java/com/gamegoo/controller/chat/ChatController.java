package com.gamegoo.controller.chat;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.ChatConverter;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
