package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.EmailCodeDTO;
import com.gamegoo.dto.member.EmailDTO;
import com.gamegoo.dto.member.JoinDTO;
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
    public ApiResponse<Object> joinMember(@RequestBody JoinDTO joinDTO) {
        authService.joinMember(joinDTO);
        return ApiResponse.onSuccess("회원가입에 성공했습니다.");
    }

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증코드 전송 API 입니다.", description = "API for sending email")
    public ApiResponse<Object> sendEmail(@RequestBody EmailDTO emailDTO) {
        System.out.println("DD");
        String email = emailDTO.getEmail();
        System.out.println(email);

        authService.sendEmail(email);
        return ApiResponse.onSuccess("인증 이메일을 발송했습니다.");
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증코드 검증 API 입니다.", description = "API for email verification")
    public ApiResponse<Object> verifyEmail(@RequestBody EmailCodeDTO emailCodeDTO) {
        String email = emailCodeDTO.getEmail();
        String code = emailCodeDTO.getCode();
        authService.verifyEmail(email, code);
        return ApiResponse.onSuccess("인증코드 검증에 성공했습니다.");
    }
}
