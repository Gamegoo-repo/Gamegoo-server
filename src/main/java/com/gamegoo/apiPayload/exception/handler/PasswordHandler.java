package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.GeneralException;

public class PasswordHandler extends GeneralException {
    public PasswordHandler(ErrorStatus code) {
        super(code);
    }
}
