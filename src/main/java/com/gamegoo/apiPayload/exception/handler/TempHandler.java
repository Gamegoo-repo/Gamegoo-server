package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {
    public TempHandler(BaseErrorCode code) {
        super(code);
    }
}
