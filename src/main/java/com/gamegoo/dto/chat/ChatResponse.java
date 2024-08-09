package com.gamegoo.dto.chat;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
        Integer targetMemberImg;
        String targetMemberName;
        String lastMsg;
        String lastMsgAt;
        Integer notReadMsgCnt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonPropertyOrder({
        "uuid",
        "memberId",
        "gameName",
        "memberProfileImg",
        "blocked",
        "chatMessageList"
    })
    public static class ChatroomEnterDTO {

        String uuid;
        Long memberId;
        String gameName;
        Integer memberProfileImg;
        boolean blocked;
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
        Integer senderProfileImg;
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
        Integer senderProfileImg;
        String message;
        String createdAt;
        Long timestamp;
    }
}
