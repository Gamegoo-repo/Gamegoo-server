package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class MannerHandler extends GeneralException {
    public MannerHandler(BaseErrorCode code) {
        super(code);
    }
}
