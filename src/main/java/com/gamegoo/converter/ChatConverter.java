package com.gamegoo.converter;

import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.util.DatetimeUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;

public class ChatConverter {

    public static ChatResponse.ChatroomCreateResultDTO toChatroomCreateResultDTO(Chatroom chatroom,
        Long targetMemberId) {

        return ChatResponse.ChatroomCreateResultDTO.builder()
            .chatroomId(chatroom.getId())
            .uuid(chatroom.getUuid())
            .targetMemberId(targetMemberId)
            .build();
    }

    public static ChatResponse.ChatCreateResultDTO toChatCreateResultDTO(Chat chat) {

        return ChatResponse.ChatCreateResultDTO.builder()
            .senderId(chat.getFromMember().getId())
            .senderProfileImg(chat.getFromMember().getProfileImage())
            .senderName(chat.getFromMember().getGameName())
            .message(chat.getContents())
            .createdAt(DatetimeUtil.toKSTString(chat.getCreatedAt()))
            .timestamp(chat.getTimestamp())
            .build();
    }

    public static ChatResponse.ChatMessageListDTO toChatMessageListDTO(Slice<Chat> chat) {
        List<ChatResponse.ChatMessageDTO> chatMessageDtoList = chat.stream()
            .map(chatElement -> {
                    if (chatElement.getFromMember().getId().equals(0L)) { // 해당 메시지가 시스템 메시지인 경우
                        return ChatConverter.toSystemMessageDTO(chatElement);
                    }
                    return ChatConverter.toChatMessageDto(chatElement);
                }
            ).collect(Collectors.toList());

        return ChatResponse.ChatMessageListDTO.builder()
            .chatMessageDtoList(chatMessageDtoList)
            .list_size(chatMessageDtoList.size())
            .has_next(chat.hasNext())
            .next_cursor(chat.hasNext() ? chat.getContent().get(0).getTimestamp()
                : null) // next cursor를 현재 chat list의 가장 오래된 chat의 timestamp로 주기
            .build();
    }

    public static ChatResponse.ChatMessageDTO toChatMessageDto(Chat chat) {

        return ChatResponse.ChatMessageDTO.builder()
            .senderId(chat.getFromMember().getId())
            .senderName(chat.getFromMember().getGameName())
            .senderProfileImg(chat.getFromMember().getProfileImage())
            .message(chat.getContents())
            .createdAt(DatetimeUtil.toKSTString(chat.getCreatedAt()))
            .timestamp(chat.getTimestamp())
            .build();

    }

    public static ChatResponse.SystemMessageDTO toSystemMessageDTO(Chat chat) {
        return ChatResponse.SystemMessageDTO.builder()
            .senderId(chat.getFromMember().getId())
            .senderName(chat.getFromMember().getGameName())
            .senderProfileImg(chat.getFromMember().getProfileImage())
            .message(chat.getContents())
            .createdAt(DatetimeUtil.toKSTString(chat.getCreatedAt()))
            .timestamp(chat.getTimestamp())
            .boardId(chat.getSourceBoard() != null ? chat.getSourceBoard().getId() : null)
            .build();
    }

}
