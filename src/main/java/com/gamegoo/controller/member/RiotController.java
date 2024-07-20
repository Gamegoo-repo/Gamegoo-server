package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.MemberRequest;
import com.gamegoo.service.member.RiotService;
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
@RequestMapping("/api/member")
@Slf4j
public class RiotController {
    private final RiotService riotService;

    @PostMapping("/riot")
    @Operation(summary = "회원가입 시 riot API를 통해 소환사명을 인증하는 API", description = "API for verifying by riot API")
    public ApiResponse<String> VerifyRiot(@RequestBody @Valid MemberRequest.verifyRiotRequestDTO verifyRiotRequestDTO) {
        String gameName = verifyRiotRequestDTO.getGame_name();
        String tag = verifyRiotRequestDTO.getTag();
        String email = verifyRiotRequestDTO.getEmail();

        riotService.updateMemberRiotInfo(gameName, tag, email);

        return ApiResponse.onSuccess("Riot 계정이 연동되었습니다.");

    }
}
