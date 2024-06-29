package com.gamegoo.apiPayload.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import io.jsonwebtoken.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class JWTExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {

            if (Objects.equals(e.getMessage(), "Token expired")) {
                setErrorResponse(response, ErrorStatus.TOKEN_EXPIRED);
            } else if (Objects.equals(e.getMessage(), "Token null")) {
                setErrorResponse(response, ErrorStatus.TOKEN_NULL);
            } else if (Objects.equals(e.getMessage(), "No Member")) {
                setErrorResponse(response, ErrorStatus.MEMBER_NOT_FOUND);
            } else {
                setErrorResponse(response, ErrorStatus.INVALID_TOKEN);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorStatus errorStatus) throws IOException {
        // 에러 응답 생성하기
        ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage(), null);
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }
}

