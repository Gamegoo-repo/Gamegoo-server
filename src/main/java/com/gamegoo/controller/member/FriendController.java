package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.Friend;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.service.member.MemberService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Friend", description = "친구 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {

    private final MemberService memberService;

    @Operation(summary = "친구 목록 조회 API", description = "해당 회원의 친구 목록을 조회하는 API 입니다.")
    @GetMapping
    public ApiResponse<Object> getFriendList() {
        Long memberId = JWTUtil.getCurrentUserId();
        List<Friend> friends = memberService.getFriends(memberId);

        List<MemberResponse.friendInfoDTO> friendInfoDTOList = friends.stream()
            .map(MemberConverter::toFriendInfoDto).collect(
                Collectors.toList());

        return ApiResponse.onSuccess(friendInfoDTOList);

    }


}
