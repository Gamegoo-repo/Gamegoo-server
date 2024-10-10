package com.gamegoo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JWTExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();  // 고유한 requestId 생성
        String requestUrl = request.getRequestURI();
        String httpMethod = request.getMethod();  // HTTP 메소드 추출
        String clientIp = getClientIp(request);
        String memberId = "Unauthenticated";  // 기본값으로 설정 (로그인 상태가 아닐 때)
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {

            if (Objects.equals(e.getMessage(), "Token expired")) {
                setErrorResponse(response, ErrorStatus.TOKEN_EXPIRED, requestId, httpMethod,
                    requestUrl, clientIp, memberId);
            } else if (Objects.equals(e.getMessage(), "Token null")) {
                setErrorResponse(response, ErrorStatus.TOKEN_NULL, requestId, httpMethod,
                    requestUrl, clientIp, memberId);
            } else if (Objects.equals(e.getMessage(), "No Member")) {
                setErrorResponse(response, ErrorStatus.MEMBER_NOT_FOUND, requestId, httpMethod,
                    requestUrl, clientIp, memberId);
            } else {
                setErrorResponse(response, ErrorStatus.INVALID_TOKEN, requestId, httpMethod,
                    requestUrl, clientIp, memberId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorStatus errorStatus,
        String requestId, String httpMethod, String requestUrl, String clientIp, String memberId)
        throws IOException {
        // 에러 응답 생성하기
        ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(),
            errorStatus.getMessage(), null);
        response.setStatus(errorStatus.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        log.info("[requestId: {}] [{}] {} | IP: {} | Member ID: {} | Status: {}", requestId,
            httpMethod, requestUrl, clientIp, memberId,
            errorStatus.getHttpStatus().value() + " " + errorStatus.getMessage());

        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }

    // 클라이언트 IP 가져오는 메소드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

