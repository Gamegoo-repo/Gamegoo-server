package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.EmailDTO;
import com.gamegoo.service.member.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/email")
    @Operation(summary = "이메일 인증코드 전송 API 입니다.", description = "API for email verification")
    public ApiResponse<Object> verifyEmail(@RequestBody EmailDTO emailDTO) throws IOException {
        String emailAddress = emailDTO.getEmail_address();
        String code = emailService.verifyEmail(emailAddress);
        return ApiResponse.onSuccess(code);
    }

}
