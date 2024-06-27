package com.gamegoo.apiPayload.exception;

import org.springframework.security.core.AuthenticationException;

public class CustomUserException extends AuthenticationException {
    public CustomUserException(String msg) {
        super(msg);
    }
}
