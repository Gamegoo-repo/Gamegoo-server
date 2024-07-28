package com.gamegoo.dto.chat;

import javax.validation.constraints.NotNull;
import lombok.Getter;

public class ChatRequest {

    @Getter
    public static class ChatroomCreateRequest {

        @NotNull
        Long targetMemberId;

        @NotNull
        String chatroomType;

        String postUrl;
    }

}
