package com.gamegoo.apiPayload.exception.handler;

import com.gamegoo.apiPayload.code.BaseErrorCode;
import com.gamegoo.apiPayload.exception.GeneralException;

public class NotificationHandler extends GeneralException {

    public NotificationHandler(BaseErrorCode code) {
        super(code);
    }
}
