package com.gamegoo.apiPayload.exception.handler;


import com.gamegoo.apiPayload.code.status.ErrorStatus;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class MemberNotFoundExceptionHandler extends AuthenticationException {
    public MemberNotFoundExceptionHandler(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
    }

}
