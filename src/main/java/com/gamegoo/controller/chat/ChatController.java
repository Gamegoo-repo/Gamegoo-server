package com.gamegoo.controller.chat;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.service.chat.ChatQueryService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1")
@Tag(name = "Chat", description = "Chat 관련 API")
public class ChatController {

    private final ChatQueryService chatQueryService;

    @Operation(summary = "채팅방 uuid 조회 API", description = "회원이 속한 채팅방의 uuid를 조회하는 API 입니다.")
    @GetMapping("/member/chatroom/uuid")
    public ApiResponse<List<String>> getChatroomUuid() {
        Long memberId = JWTUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        List<String> chatroomUuids = chatQueryService.getChatroomUuids(memberId);
        return ApiResponse.onSuccess(chatroomUuids);
    }

}
