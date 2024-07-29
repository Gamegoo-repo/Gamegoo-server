package com.gamegoo.converter;

import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.dto.chat.ChatResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;

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

    public static ChatResponse.ChatMessageListDTO toChatMessageListDTO(Slice<Chat> chat) {
        List<ChatResponse.ChatMessageDTO> chatMessageDtoList = chat.stream()
            .map(ChatConverter::toChatMessageDto).collect(Collectors.toList());

        return ChatResponse.ChatMessageListDTO.builder()
            .chatMessageDtoList(chatMessageDtoList)
            .list_size(chatMessageDtoList.size())
            .has_next(chat.hasNext())
            .next_cursor(chat.hasNext() ? chat.getContent().get(0).getTimestamp()
                : null) // next cursor를 현재 chat list의 가장 오래된 chat의 timestamp로 주기
            .build();
    }

    public static ChatResponse.ChatMessageDTO toChatMessageDto(Chat chat) {
        // ISO 8601 형식의 문자열로 변환
        String createdAtIoString = chat.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);

        return ChatResponse.ChatMessageDTO.builder()
            .senderId(chat.getFromMember().getId())
            .senderName(chat.getFromMember().getGameName())
            .senderProfileImg(chat.getFromMember().getProfileImage())
            .message(chat.getContents())
            .createdAt(createdAtIoString)
            .timestamp(chat.getTimestamp())
            .build();

    }

}
