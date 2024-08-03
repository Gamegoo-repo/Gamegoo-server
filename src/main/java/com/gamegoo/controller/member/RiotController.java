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
@RequestMapping("/v1/member")
@Slf4j
public class RiotController {

    private final RiotService riotService;

    @PostMapping("/riot")
    @Operation(summary = "실제 존재하는 Riot 계정인지 검증하는 API", description = "API for verifying account by riot API")
    public ApiResponse<String> VerifyRiot(
            @RequestBody @Valid MemberRequest.verifyRiotRequestDTO verifyRiotRequestDTO) {
        System.out.println("RIOT_API 호출");
        String gameName = verifyRiotRequestDTO.getGameName();
        String tag = verifyRiotRequestDTO.getTag();

        riotService.verifyRiot(gameName, tag);

        return ApiResponse.onSuccess("해당 Riot 계정은 존재합니다.");

    }

}
