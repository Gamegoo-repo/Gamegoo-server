package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.MemberRequest;
import com.gamegoo.service.member.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/join")
    @Operation(summary = "회원가입 API 입니다.", description = "API for join")
    public ApiResponse<String> joinMember(@RequestBody MemberRequest.JoinRequestDTO joinRequestDTO) {
        authService.joinMember(joinRequestDTO);
        return ApiResponse.onSuccess("회원가입에 성공했습니다.");
    }

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증코드 전송 API 입니다.", description = "API for sending email")
    public ApiResponse<String> sendEmail(@RequestBody MemberRequest.EmailRequestDTO emailRequestDTO) {
        String email = emailRequestDTO.getEmail();
        authService.sendEmail(email);
        return ApiResponse.onSuccess("인증 이메일을 발송했습니다.");
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증코드 검증 API 입니다.", description = "API for email verification")
    public ApiResponse<String> verifyEmail(@RequestBody MemberRequest.EmailCodeRequestDTO emailCodeRequestDTO) {
        String email = emailCodeRequestDTO.getEmail();
        String code = emailCodeRequestDTO.getCode();
        authService.verifyEmail(email, code);
        return ApiResponse.onSuccess("인증코드 검증에 성공했습니다.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh token을 통한 access, refresh token 재발급 API 입니다.", description = "API for Refresh Token")
    public ApiResponse<Object> refreshTokens(@RequestBody MemberRequest.RefreshTokenRequestDTO refreshTokenRequestDTO) {

        String refreshToken = refreshTokenRequestDTO.getRefresh_token();

        MemberRequest.RefreshTokenResponseDTO refreshTokenResponseDTO = authService.verifyRefreshToken(refreshToken);

        return ApiResponse.onSuccess(refreshTokenResponseDTO);
    }
}
