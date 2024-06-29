package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class BlockHandler extends GeneralException {
    public BlockHandler(BaseErrorCode code) {
        super(code);
    }
}
