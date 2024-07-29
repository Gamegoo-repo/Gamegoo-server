package com.gamegoo.converter;

import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatResponse;

public class ChatConverter {

    public static ChatResponse.ChatroomCreateResultDto toChatroomCreateResultDto(Chatroom chatroom,
        Long targetMemberId) {

        return ChatResponse.ChatroomCreateResultDto.builder()
            .chatroomId(chatroom.getId())
            .uuid(chatroom.getUuid())
            .postUrl(chatroom.getPostUrl())
            .targetMemberId(targetMemberId)
            .build();
    }

}
