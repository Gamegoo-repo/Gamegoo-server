package com.gamegoo.converter;

import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatResponse;
import java.time.format.DateTimeFormatter;

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

    public static ChatResponse.ChatCreateResultDTO toChatCreateResultDTO(Chat chat) {
        // ISO 8601 형식의 문자열로 변환
        String createdAtIoString = chat.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);

        return ChatResponse.ChatCreateResultDTO.builder()
            .senderId(chat.getFromMember().getId())
            .senderProfileImg(chat.getFromMember().getProfileImage())
            .senderName(chat.getFromMember().getGameName())
            .message(chat.getContents())
            .createdAt(createdAtIoString)
            .timestamp(chat.getTimestamp())
            .build();
    }

}
