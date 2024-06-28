package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.PasswordDTO;
import com.gamegoo.security.SecurityUtil;
import com.gamegoo.service.member.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/password")
@Slf4j
public class PasswordController {
    private final PasswordService passwordService;

    @PostMapping("/check")
    @Operation(summary = "비밀번호 확인 API 입니다.", description = "API for checking password")
    public ApiResponse<Object> checkPassword(@RequestBody PasswordDTO passwordDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드

        boolean isPasswordValid = passwordService.checkPasswordById(currentUserId, passwordDTO.getPassword()); //request body에 있는 password와 currentUserId를 전달

        if (isPasswordValid) {
            return ApiResponse.onSuccess(null);
        } else {
            return ApiResponse.onFailure("PASSWORD_INVALID", "비밀번호가 불일치합니다.", null);
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "비밀번호 재설정 API 입니다.", description = "API for reseting password")
    public ApiResponse<Object> resetPassword(@RequestBody PasswordDTO passwordDTO) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        passwordService.updatePassword(currentUserId, passwordDTO.getPassword());

        return ApiResponse.onSuccess(null);
    }


}
