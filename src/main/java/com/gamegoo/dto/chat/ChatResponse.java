package com.gamegoo.dto.chat;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatroomCreateResultDTO {

        Long chatroomId;
        String uuid;
        String postUrl;
        Long targetMemberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatroomViewDTO {

        Long chatroomId;
        String uuid;
        String targetMemberImg;
        String targetMemberName;
        String lastMsg;
        String lastMsgAt;
        Integer notReadMsgCnt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatroomEnterDTO {

        Long memberId;
        String gameName;
        String memberProfileImg;
        ChatMessageListDTO chatMessageList;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageListDTO {

        List<ChatMessageDTO> chatMessageDtoList;
        Integer list_size;
        Boolean has_next;
        Long next_cursor;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDTO {

        Long senderId;
        String senderName;
        String senderProfileImg;
        String message;
        String createdAt;
        Long timestamp;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCreateResultDTO {

        Long senderId;
        String senderName;
        String senderProfileImg;
        String message;
        String createdAt;
        Long timestamp;
    }
}
