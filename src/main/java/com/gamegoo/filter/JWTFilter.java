package com.gamegoo.filter;

import com.gamegoo.security.CustomUserDetails;
import com.gamegoo.service.member.CustomUserDetailService;
import com.gamegoo.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// 모든 HTTP 요청마다 실행됨
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final List<String> excludedPaths;

    private final CustomUserDetailService customUserDetailService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, List<String> excludedPaths, CustomUserDetailService customUserDetailService) {
        this.jwtUtil = jwtUtil;
        this.excludedPaths = excludedPaths;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, JwtException {
        String requestURI = request.getRequestURI();

        // JWT Filter를 사용하지 않는 Path는 제외
        if (excludedPaths.stream().anyMatch(requestURI::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // request 헤더에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new JwtException("Token null");
        }

        System.out.println("authorization now");

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];
        try {
            // jwt 토큰에서 id 획득
            Long id = jwtUtil.getId(token);
            System.out.println(id);

            // UserDetails에 회원 정보 객체 담기
            CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailService.loadUserById(id);

            // 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            // 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired");
        } catch (JwtException e) {
            throw new JwtException("Invalid token");
        }

    }

}
