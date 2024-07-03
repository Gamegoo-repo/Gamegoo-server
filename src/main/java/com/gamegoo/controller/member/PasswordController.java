package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.dto.member.MemberRequestDTO;
import com.gamegoo.service.member.PasswordService;
import com.gamegoo.util.JWTUtil;
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
    public ApiResponse<String> checkPassword(@RequestBody MemberRequestDTO.PasswordRequestDTO passwordRequestDTO) {
        Long currentUserId = JWTUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드

        boolean isPasswordValid = passwordService.checkPasswordById(currentUserId, passwordRequestDTO.getPassword()); //request body에 있는 password와 currentUserId를 전달

        if (isPasswordValid) {
            return ApiResponse.onSuccess("비밀번호가 일치합니다.");
        } else {
            throw new MemberHandler(ErrorStatus.PASSWORD_INVALID);
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "비밀번호 재설정 API 입니다.", description = "API for reseting password")
    public ApiResponse<String> resetPassword(@RequestBody MemberRequestDTO.PasswordRequestDTO passwordRequestDTO) {
        Long currentUserId = JWTUtil.getCurrentUserId();

        passwordService.updatePassword(currentUserId, passwordRequestDTO.getPassword());

        return ApiResponse.onSuccess("비밀번호 재설정을 완료했습니다.");
    }


}
