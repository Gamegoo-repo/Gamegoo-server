package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.Member;
import com.gamegoo.security.SecurityUtil;
import com.gamegoo.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;


    @Operation(summary = "회원 차단 API", description = "대상 회원을 차단하는 API 입니다.")
    @Parameter(name = "memberId", description = "차단할 대상 회원의 id 입니다.")
    @PostMapping("/bock/{memberId}")
    public ApiResponse<String> blockMember(@PathVariable(name = "memberId") Long targetMemberId) {
        Long memberId = SecurityUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        Member member = memberService.blockMember(memberId, targetMemberId);

        return ApiResponse.onSuccess("회원 차단 성공");
    }
}
