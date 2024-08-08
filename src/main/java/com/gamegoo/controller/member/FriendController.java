package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.service.member.FriendService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Friend", description = "친구 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 목록 조회 API", description = "해당 회원의 친구 목록을 조회하는 API 입니다.")
    @GetMapping
    public ApiResponse<List<MemberResponse.friendInfoDTO>> getFriendList() {
        Long memberId = JWTUtil.getCurrentUserId();
        List<Friend> friends = friendService.getFriends(memberId);

        List<MemberResponse.friendInfoDTO> friendInfoDTOList = friends.stream()
            .map(MemberConverter::toFriendInfoDto).collect(
                Collectors.toList());

        return ApiResponse.onSuccess(friendInfoDTOList);

    }

    @Operation(summary = "친구 요청 전송 API", description = "대상 회원에게 친구 요청을 전송하는 API 입니다."
        + "대상 회원에게 친구 요청 알림을 전송하며, 대상 회원이 현재 접속 중인 경우 socket을 통해 실시간 알림을 전송합니다.")
    @Parameter(name = "memberId", description = "친구 요청을 전송할 대상 회원의 id 입니다.")
    @PostMapping("/send/{memberId}")
    public ApiResponse<String> sendFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.sendFriendRequest(memberId, targetMemberId);

        return ApiResponse.onSuccess("친구 요청 전송 성공");

    }

    @Operation(summary = "친구 요청 수락 API", description = "대상 회원이 보낸 친구 요청을 수락 처리하는 API 입니다.")
    @Parameter(name = "memberId", description = "친구 요청을 수락할 대상 회원의 id 입니다.")
    @GetMapping("/request/{memberId}/accept")
    public ApiResponse<String> acceptFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.acceptFriendRequest(memberId, targetMemberId);

        return ApiResponse.onSuccess("친구 요청 수락 성공");
    }


}
