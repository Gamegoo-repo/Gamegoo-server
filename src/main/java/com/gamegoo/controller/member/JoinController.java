package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.JoinDTO;
import com.gamegoo.service.member.JoinService;
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
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join")
    @Operation(summary = "회원가입 API 입니다.", description = "API for join")
    public ApiResponse<Object> joinMember(@RequestBody JoinDTO joinDTO) {
        System.out.println(joinDTO.getPassword());
        System.out.println(joinDTO.getEmail());
        joinService.JoinMember(joinDTO);
        return ApiResponse.onSuccess(null);
    }

}
