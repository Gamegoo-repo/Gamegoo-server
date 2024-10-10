package com.gamegoo.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.util.JWTUtil;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public LoggingFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        // /v1/member/login 경로에 대해서는 필터 pass
        String requestUrl = request.getRequestURI();
        if ("/v1/member/login".equals(requestUrl)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();  // 고유한 requestId 생성

        try {
            MDC.put("requestId", requestId);

            // 요청 정보 추출
            String httpMethod = request.getMethod(); // HTTP 메소드 추출
            String clientIp = getClientIp(request);
            String jwtToken = extractJwtToken(request);
            String memberId = null;
            boolean jwtTokenPresent = jwtToken != null;
            String params = getParamsAsString(request);

            // 토큰이 있을 경우 사용자 ID 추출
            if (jwtTokenPresent) {
                try {
                    memberId = jwtUtil.getId(jwtToken).toString();
                } catch (JwtException e) {
                    log.error("JWT Exception: {}", e.getMessage());
                    memberId = "Invalid JWT";  // JWT 에러가 있을 경우
                }
            } else {
                memberId = "Unauthenticated";  // 비로그인 사용자
            }

            // 요청 로그 기록
            if (params != null && !params.isEmpty() && !params.equals("{}")) {
                log.info("[requestId: {}] [{}] {} | IP: {} | Member ID: {} | Params: {}", requestId,
                    httpMethod, requestUrl, clientIp, memberId, params);
            } else {
                log.info("[requestId: {}] [{}] {} | IP: {} | Member ID: {}", requestId,
                    httpMethod, requestUrl, clientIp, memberId);
            }

            // 실행 시간 측정을 위한 시작 시간
//        long startTime = System.currentTimeMillis();

            // 요청 처리
            filterChain.doFilter(request, response);

            // 응답 정보 추출
//        long executionTime = System.currentTimeMillis() - startTime;
            int statusCode = response.getStatus();
            String statusMessage = getStatusMessage(statusCode);

            // 응답 로그 기록
            log.info("[requestId: {}] [{}] {} | IP: {} | Member ID: {} | Status: {}", requestId,
                httpMethod, requestUrl,
                clientIp, memberId, statusMessage);
        } finally {
            MDC.remove("requestId");
        }
    }

    // JWT 토큰을 Authorization 헤더에서 추출하는 메서드
    private String extractJwtToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    // 클라이언트 IP 가져오는 메소드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // 상태 코드에 맞는 메시지 반환
    private String getStatusMessage(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        return httpStatus != null ? statusCode + " " + httpStatus.getReasonPhrase()
            : String.valueOf(statusCode);
    }

    private String getParamsAsString(HttpServletRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(request.getParameterMap());
        } catch (JsonProcessingException e) {
            return "Unable to parse parameters";
        }
    }
}
