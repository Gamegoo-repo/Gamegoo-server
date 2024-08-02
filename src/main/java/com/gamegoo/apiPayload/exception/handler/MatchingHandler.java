package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class MatchingHandler extends GeneralException {
    public MatchingHandler(BaseErrorCode code) {
        super(code);
    }
}
