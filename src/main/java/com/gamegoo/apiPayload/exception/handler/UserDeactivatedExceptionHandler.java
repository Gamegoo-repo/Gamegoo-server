package com.gamegoo.apiPayload.exception.handler;


import com.gamegoo.apiPayload.code.status.ErrorStatus;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class UserDeactivatedExceptionHandler extends AuthenticationException {

    public UserDeactivatedExceptionHandler(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
    }
}
