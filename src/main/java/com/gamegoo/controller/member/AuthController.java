package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.member.MemberRequest;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.service.member.AuthService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    @Operation(summary = "회원가입 API 입니다.", description = "API for join")
    public ApiResponse<MemberResponse.myProfileMemberDTO> joinMember(
            @RequestBody @Valid MemberRequest.JoinRequestDTO joinRequestDTO) {
        String email = joinRequestDTO.getEmail();
        String password = joinRequestDTO.getPassword();
        String gameName = joinRequestDTO.getGameName();
        String tag = joinRequestDTO.getTag();
        Boolean isAgree = joinRequestDTO.getIsAgree();

        Member member = authService.joinMember(email, password, gameName, tag, isAgree);
        return ApiResponse.onSuccess(MemberConverter.toMyProfileDTO(member));
    }

    @PostMapping("/email/send/join")
    @Operation(summary = "회원가입용 이메일 인증코드 전송 API 입니다. 중복확인 포함", description = "API for sending email for join")
    public ApiResponse<String> sendEmailwithCheckDuplication(
            @RequestBody MemberRequest.EmailRequestDTO emailRequestDTO) {
        String email = emailRequestDTO.getEmail();
        authService.sendEmail(email, true);
        return ApiResponse.onSuccess("인증 이메일을 발송했습니다.");
    }

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증코드 전송 API 입니다. 중복확인 X", description = "API for sending email")
    public ApiResponse<String> sendEmail(
            @RequestBody MemberRequest.EmailRequestDTO emailRequestDTO) {
        String email = emailRequestDTO.getEmail();
        authService.sendEmail(email, false);
        return ApiResponse.onSuccess("인증 이메일을 발송했습니다.");
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증코드 검증 API 입니다.", description = "API for email verification")
    public ApiResponse<String> verifyEmail(
            @RequestBody MemberRequest.EmailCodeRequestDTO emailCodeRequestDTO) {
        String email = emailCodeRequestDTO.getEmail();
        String code = emailCodeRequestDTO.getCode();
        authService.verifyEmail(email, code);
        return ApiResponse.onSuccess("인증코드 검증에 성공했습니다.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh token을 통한 access, refresh token 재발급 API 입니다.", description = "API for Refresh Token")
    public ApiResponse<Object> refreshTokens(
            @RequestBody MemberRequest.RefreshTokenRequestDTO refreshTokenRequestDTO) {

        String refreshToken = refreshTokenRequestDTO.getRefreshToken();

        MemberResponse.RefreshTokenResponseDTO refreshTokenResponseDTO = authService.verifyRefreshToken(
                refreshToken);

        return ApiResponse.onSuccess(refreshTokenResponseDTO);
    }

    @PostMapping("/logout")
    @Operation(summary = "logout API 입니다.", description = "API for logout")
    public ApiResponse<String> logoutMember() {
        Long memberId = JWTUtil.getCurrentUserId();
        authService.logoutMember(memberId);

        return ApiResponse.onSuccess("로그아웃에 성공했습니다");
    }
}
