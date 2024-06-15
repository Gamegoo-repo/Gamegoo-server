package com.gamegoo.controller.member;

import com.gamegoo.dto.member.JoinDTO;
import com.gamegoo.service.JoinService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join/local")
    @Operation(summary = "회원가입 API 입니다.", description = "API for join")
    public String joinProcess(JoinDTO joinDTO) {
        joinService.JoinProcess(joinDTO);
        return "join success";
    }

}
