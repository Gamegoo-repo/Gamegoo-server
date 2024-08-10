package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class SocketHandler extends GeneralException {

    public SocketHandler(BaseErrorCode code) {
        super(code);
    }
}
