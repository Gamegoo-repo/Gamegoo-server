package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.dto.member.MemberResponse.friendRequestResultDTO;
import com.gamegoo.dto.member.MemberResponse.starFriendResultDTO;
import com.gamegoo.service.member.FriendService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Friend", description = "친구 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 목록 조회 API", description =
        "해당 회원의 친구 목록을 조회하는 API 입니다. 이름 오름차순(한글-영문-숫자 순)으로 정렬해 제공합니다.\n\n"
            + "cursor를 보내지 않으면 상위 10개 친구 목록을 조회합니다.")
    @Parameter(name = "cursor", description = "페이징을 위한 커서, 이전 친구 목록 조회에서 응답받은 next_cursor를 보내주세요.")
    @GetMapping
    public ApiResponse<MemberResponse.friendListDTO> getFriendList(
        @RequestParam(name = "cursor", required = false) Long cursorId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Slice<Friend> friends = friendService.getFriends(memberId, cursorId);

        return ApiResponse.onSuccess(MemberConverter.toFriendListDTO(friends));

    }

    @Operation(summary = "친구 요청 전송 API", description = "대상 회원에게 친구 요청을 전송하는 API 입니다."
        + "대상 회원에게 친구 요청 알림을 전송하며, 대상 회원이 현재 접속 중인 경우 socket을 통해 실시간 알림을 전송합니다.")
    @Parameter(name = "memberId", description = "친구 요청을 전송할 대상 회원의 id 입니다.")
    @PostMapping("/request/{memberId}")
    public ApiResponse<MemberResponse.friendRequestResultDTO> sendFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.sendFriendRequest(memberId, targetMemberId);

        MemberResponse.friendRequestResultDTO result = friendRequestResultDTO.builder()
            .targetMemberId(targetMemberId)
            .result("친구 요청 전송 성공")
            .build();

        return ApiResponse.onSuccess(result);

    }

    @Operation(summary = "친구 요청 취소 API", description = "대상 회원에게 보낸 친구 요청을 취소하는 API 입니다.")
    @Parameter(name = "memberId", description = "친구 요청을 취소할 대상 회원의 id 입니다.")
    @DeleteMapping("/request/{memberId}")
    public ApiResponse<MemberResponse.friendRequestResultDTO> cancelFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.cancelFriendRequest(memberId, targetMemberId);

        MemberResponse.friendRequestResultDTO result = friendRequestResultDTO.builder()
            .targetMemberId(targetMemberId)
            .result("친구 요청 취소 성공")
            .build();

        return ApiResponse.onSuccess(result);

    }

    @Operation(summary = "친구 요청 수락 API", description = "대상 회원이 보낸 친구 요청을 수락 처리하는 API 입니다.")
    @Parameter(name = "memberId", description = "친구 요청을 수락할 대상 회원의 id 입니다.")
    @PatchMapping("/request/{memberId}/accept")
    public ApiResponse<MemberResponse.friendRequestResultDTO> acceptFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.acceptFriendRequest(memberId, targetMemberId);

        MemberResponse.friendRequestResultDTO result = friendRequestResultDTO.builder()
            .targetMemberId(targetMemberId)
            .result("친구 요청 수락 성공")
            .build();

        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 요청 거절 API", description = "대상 회원이 보낸 친구 요청을 거절 처리하는 API 입니다.")
    @Parameter(name = "memberId", description = "친구 요청을 거절할 대상 회원의 id 입니다.")
    @PatchMapping("/request/{memberId}/reject")
    public ApiResponse<MemberResponse.friendRequestResultDTO> rejectFriendRequest(
        @PathVariable(name = "memberId") Long targetMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.rejectFriendRequest(memberId, targetMemberId);

        MemberResponse.friendRequestResultDTO result = friendRequestResultDTO.builder()
            .targetMemberId(targetMemberId)
            .result("친구 요청 거절 성공")
            .build();

        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 즐겨찾기 설정 API", description = "대상 친구 회원을 즐겨찾기 설정하는 API 입니다.")
    @Parameter(name = "memberId", description = "즐겨찾기 설정할 친구 회원의 id 입니다.")
    @PatchMapping("/{memberId}/star")
    public ApiResponse<MemberResponse.starFriendResultDTO> starFriend(
        @PathVariable(name = "memberId") Long friendMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Friend friend = friendService.starFriend(memberId, friendMemberId);

        starFriendResultDTO result = starFriendResultDTO.builder()
            .friendMemberId(friend.getToMember().getId())
            .result("친구 즐겨찾기 설정 성공")
            .build();

        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 즐겨찾기 해제 API", description = "대상 친구 회원을 즐겨찾기 해제하는 API 입니다.")
    @Parameter(name = "memberId", description = "즐겨찾기 해제할 친구 회원의 id 입니다.")
    @DeleteMapping("/{memberId}/star")
    public ApiResponse<MemberResponse.starFriendResultDTO> unstarFriend(
        @PathVariable(name = "memberId") Long friendMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Friend friend = friendService.unstarFriend(memberId, friendMemberId);

        starFriendResultDTO result = starFriendResultDTO.builder()
            .friendMemberId(friend.getToMember().getId())
            .result("친구 즐겨찾기 해제 성공")
            .build();

        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "친구 삭제 API", description = "친구 회원과의 친구 관계를 끊는 API 입니다.")
    @Parameter(name = "memberId", description = "삭제 처리할 친구 회원의 id 입니다.")
    @DeleteMapping("/{memberId}")
    public ApiResponse<String> deleteFriend(
        @PathVariable(name = "memberId") Long friendMemberId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        friendService.deleteFriend(memberId, friendMemberId);
        return ApiResponse.onSuccess("친구 삭제 성공");
    }
}
