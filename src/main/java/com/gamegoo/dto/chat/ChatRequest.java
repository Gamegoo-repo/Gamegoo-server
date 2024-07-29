package com.gamegoo.dto.chat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;

public class ChatRequest {

    @Getter
    public static class ChatroomCreateRequest {

        @NotNull
        Long targetMemberId;

        String postUrl;
    }

    @Getter
    public static class ChatCreateRequest {

        @NotEmpty
        String message;
    }

}
