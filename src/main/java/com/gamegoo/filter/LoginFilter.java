package com.gamegoo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.CustomUserDetails;
import com.gamegoo.util.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@ResponseBody
// 로그인 시 실행되는 로그인 필터
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, MemberRepository memberRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/member/login", "POST"));
        this.setUsernameParameter("email");
        this.setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 클라이언트 요청에서 username, password 추출
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

        // token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공시 실행하는 메소드 (JWT 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 id 불러오기
        Long id = customUserDetails.getId();

        // jwt 토큰 생성
        String access_token = jwtUtil.createJwtWithId(id, 60 * 60 * 1000L);     // 1시간
        String refresh_token = jwtUtil.createJwt(60 * 60 * 24 * 30 * 1000L);    // 30일

        // refresh token DB에 저장하기
        Member member = memberRepository.findById(id)
                .orElseThrow();
        member.setRefreshToken(refresh_token);
        memberRepository.save(member);

        // 해당 유저 이름 불러오기
        String gameuserName = member.getGameName();

        // Response body에 넣기
        MemberResponse.LoginResponseDTO loginResponseDTO = new MemberResponse.LoginResponseDTO(access_token, refresh_token, gameuserName);

        // 헤더에 추가
        response.addHeader("Authorization", "Bearer " + access_token);

        // 성공 응답 생성
        ApiResponse<Object> apiResponse = ApiResponse.onSuccess(loginResponseDTO);

        // 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        apiResponse(response, apiResponse);
    }

    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        // 어떤 에러인지 확인
        ErrorStatus errorStatus = getErrorStatus(failed);

        // 에러 응답 생성하기
        ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage(), null);
        response.setStatus(errorStatus.getHttpStatus().value());
        apiResponse(response, apiResponse);
    }

    private static void apiResponse(HttpServletResponse response, ApiResponse<Object> apiResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }

    private static ErrorStatus getErrorStatus(AuthenticationException failed) {

        if (Objects.equals(failed.getMessage(), "해당 사용자는 탈퇴한 사용자입니다.")) {
            return ErrorStatus.USER_DEACTIVATED;
        } else if (failed instanceof InternalAuthenticationServiceException) {
            return ErrorStatus.MEMBER_NOT_FOUND;
        } else if (Objects.equals(failed.getMessage(), "자격 증명에 실패하였습니다.")) {
            return ErrorStatus.PASSWORD_INVALID;
        } else {
            return ErrorStatus._UNAUTHORIZED;
        }
    }
}
