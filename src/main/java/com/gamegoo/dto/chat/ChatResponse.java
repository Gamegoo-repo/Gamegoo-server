package com.gamegoo.dto.chat;

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
        String postUrl;
        Long targetMemberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatroomViewDto {

        Long chatroomId;
        String uuid;
        String targetMemberImg;
        String targetMemberName;
        String lastMsg;
        String lastMsgAt;
        Integer notReadMsgCnt;
    }
}
