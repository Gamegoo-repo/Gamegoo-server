package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class ChatHandler extends GeneralException {

    public ChatHandler(BaseErrorCode code) {
        super(code);
    }
}
