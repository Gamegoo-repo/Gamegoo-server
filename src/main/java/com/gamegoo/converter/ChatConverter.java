package com.gamegoo.converter;

import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatResponse;

public class ChatConverter {

    public static ChatResponse.ChatroomCreateResultDTO toChatroomCreateResultDTO(Chatroom chatroom,
        Long targetMemberId) {

        return ChatResponse.ChatroomCreateResultDTO.builder()
            .chatroomId(chatroom.getId())
            .uuid(chatroom.getUuid())
            .postUrl(chatroom.getPostUrl())
            .targetMemberId(targetMemberId)
            .build();
    }

}
