package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.UserDeactivatedExceptionHandler;
import com.gamegoo.security.SecurityUtil;
import com.gamegoo.service.member.DeleteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class DeleteController {

    private final DeleteService deleteService;

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴 API 입니다.", description = "API for blind Member")
    public ApiResponse<Object> blindMember() {
        Long userId = SecurityUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        try {
            deleteService.deleteMember(userId);
            return ApiResponse.onSuccess(null);
        } catch (UserDeactivatedExceptionHandler e) {
            return ApiResponse.onFailure(ErrorStatus.MEMBER_NOT_FOUND.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unexpected error occurred during member deletion", e);
            return ApiResponse.onFailure(ErrorStatus.TEMP_EXCEPTION.getCode(), ErrorStatus.TEMP_EXCEPTION.getMessage(), null);
        }
    }


}
