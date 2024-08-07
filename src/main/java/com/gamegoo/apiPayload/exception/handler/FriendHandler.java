package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class FriendHandler extends GeneralException {

    public FriendHandler(BaseErrorCode code) {
        super(code);
    }
}
