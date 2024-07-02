package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.Member;
import com.gamegoo.service.member.MemberService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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
        Long memberId = JWTUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        memberService.blockMember(memberId, targetMemberId);

        return ApiResponse.onSuccess("회원 차단 성공");
    }

    @Operation(summary = "차단한 회원 목록 조회 API", description = "내가 차단한 회원의 목록을 조회하는 API 입니다.")
    @Parameter(name = "page", description = "페이지 번호, 1 이상의 숫자를 입력해 주세요.")
    @GetMapping("/block")
    public ApiResponse<Object> getBlockList(@RequestParam(name = "page") Integer page) {
        Long memberId = JWTUtil.getCurrentUserId();

        Page<Member> blockList = memberService.getBlockList(memberId, page - 1);

        return ApiResponse.onSuccess(MemberConverter.toBlockListDto(blockList));
    }
}
