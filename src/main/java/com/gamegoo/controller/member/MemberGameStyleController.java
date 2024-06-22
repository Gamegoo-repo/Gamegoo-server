package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.GameStyleDTO;
import com.gamegoo.service.member.MemberGameStyleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class MemberGameStyleController {
    private final MemberGameStyleService memberGameStyleService;

    @PutMapping("/gamestyle")
    @Operation(summary = "gamestyle 추가 및 수정 API 입니다.", description = "API for Gamestyle addition and modification ")
    public ApiResponse<Object> addGameStyle(GameStyleDTO gameStyleDTO) {
        memberGameStyleService.addMemberGameStyles(gameStyleDTO.getGamestyle());
        return ApiResponse.onSuccess(null);
    }
}
