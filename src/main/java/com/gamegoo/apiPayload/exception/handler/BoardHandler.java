package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class BoardHandler extends GeneralException {
    public BoardHandler(BaseErrorCode code) {
        super(code);
    }
}
