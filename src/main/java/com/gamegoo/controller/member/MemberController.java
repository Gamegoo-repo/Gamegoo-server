package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.Member;
import com.gamegoo.security.SecurityUtil;
import com.gamegoo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/bock/{memberId}")
    public ApiResponse<String> blockMember(@PathVariable(name = "memberId") Long targetMemberId) {
        Long memberId = SecurityUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        Member member = memberService.blockMember(memberId, targetMemberId);

        return ApiResponse.onSuccess("회원 차단 성공");
    }
}
