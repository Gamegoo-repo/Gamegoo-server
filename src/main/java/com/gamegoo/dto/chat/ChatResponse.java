package com.gamegoo.dto.chat;

import com.gamegoo.domain.enums.ChatroomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatroomCreateResultDto {

        Long chatroomId;
        String uuid;
        ChatroomType chatroomType;
        String postUrl;
        Long targetMemberId;
    }

}