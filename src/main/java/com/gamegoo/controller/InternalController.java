package com.gamegoo.controller;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MatchingHandler;
import com.gamegoo.converter.ChatConverter;
import com.gamegoo.converter.MatchingConverter;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.matching.MatchingType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.dto.matching.MatchingResponse.PriorityMatchingResponseDTO;
import com.gamegoo.dto.matching.MemberPriority;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
import com.gamegoo.service.matching.MatchingService;
import com.gamegoo.service.member.FriendService;
import com.gamegoo.service.member.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1")
@Tag(name = "Internal", description = "3000 서버용 API")
public class InternalController {

    private final MatchingService matchingService;
    private final ChatCommandService chatCommandService;
    private final ProfileService profileService;
    private final FriendService friendService;

    private final ChatQueryService chatQueryService;

    @Operation(summary = "채팅방 uuid 조회 API", description = "회원이 속한 채팅방의 uuid를 조회하는 API 입니다.")
    @GetMapping("/{memberId}/chatroom/uuid")
    public ApiResponse<List<String>> getChatroomUuid(
        @PathVariable(name = "memberId") Long memberId) {

        List<String> chatroomUuids = chatQueryService.getChatroomUuids(memberId);
        return ApiResponse.onSuccess(chatroomUuids);
    }

    @Operation(summary = "채팅 메시지 등록 API", description = "새로운 채팅 메시지를 등록하는 API 입니다.")
    @PostMapping("/{memberId}/chat/{chatroomUuid}")
    public ApiResponse<ChatResponse.ChatCreateResultDTO> addChat(
        @PathVariable(name = "memberId") Long memberId,
        @PathVariable(name = "chatroomUuid") String chatroomUuid,
        @RequestBody @Valid ChatRequest.ChatCreateRequest request
    ) {

        Chat chat = chatCommandService.addChat(request, chatroomUuid, memberId);

        return ApiResponse.onSuccess(ChatConverter.toChatCreateResultDTO(chat));
    }


    @Operation(summary = "모든 친구 id 조회 API", description = "해당 회원의 모든 친구 id 목록을 조회하는 API 입니다.\n\n"
        + "정렬 기능 없음, socket서버용 API 입니다.")
    @GetMapping("/{memberId}/friends/ids")
    public ApiResponse<List<Long>> getFriendIds(@PathVariable(name = "memberId") Long memberId) {

        return ApiResponse.onSuccess(friendService.getFriendIds(memberId));

    }

    @PostMapping("/{memberId}/matching/priority")
    @Operation(summary = "우선순위 계산 및 매칭 기록을 저장하는 API 입니다.", description =
        "API for calculating and recording matching \n\n"
            + "gameMode: 1 ~ 4 int를 넣어주세요. (1: 빠른 대전, 2: 솔로 랭크, 3: 자유 랭크, 4: 칼바람 나락) \n\n"
            + "mike: true 또는 false를 넣어주세요. \n\n"
            + "matchingType: \"BASIC\" 또는 \"PRECISE\"를 넣어주세요.\n\n"
            + "mainP: 0 ~ 5 int를 넣어주세요. \n\n"
            + "subP: 0 ~ 5 int를 넣어주세요. \n\n"
            + "wantP: 0 ~ 5 int를 넣어주세요. \n\n"
            + "gameStyleList: 1 ~ 17 int를 넣어주세요.")

    public ApiResponse<PriorityMatchingResponseDTO> saveMatching(
        @PathVariable(name = "memberId") Long memberId,
        @RequestBody @Valid MatchingRequest.InitializingMatchingRequestDTO request) {

        // 매칭 타입 유효성 검사
        try {
            MatchingType.valueOf(request.getMatchingType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MatchingHandler(ErrorStatus.MATCHING_TYPE_BAD_REQUEST);
        }

        // 우선순위 계산 리스트를 Service로부터 가져옴
        Map<String, List<MemberPriority>> priorityLists = matchingService.calculatePriorityList(
            request, memberId);

        // 각 우선순위 리스트 추출
        List<MemberPriority> myPriorityList = priorityLists.get("myPriorityList");
        List<MemberPriority> otherPriorityList = priorityLists.get("otherPriorityList");

        // DB에 기록하기
        matchingService.save(request, memberId);

        // gameStyleList 가져오기
        Member member = profileService.findMember(memberId);
        List<String> gameStyleList = profileService.getGameStyleList(member);

        // ApiResponse로 변환하여 반환
        return ApiResponse.onSuccess(MatchingConverter.toPriorityMatchingResponseDTO(
            member, request, myPriorityList, otherPriorityList, gameStyleList));
    }

    @PatchMapping("/{memberId}/matching/status")
    @Operation(summary = "나의 매칭 기록 상태(status)를 수정하는 API입니다.", description = "API for matching status modification")
    public ApiResponse<String> modifyMatching(
        @PathVariable(name = "memberId") Long memberId,
        @RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request) {

        matchingService.updateMyStatus(request, memberId);
        return ApiResponse.onSuccess("매칭 상태 변경에 성공했습니다.");
    }

    @PatchMapping("/{memberId}/matching/status/target/{targetMemberId}")
    @Parameter(name = "targetMemberId", description = "매칭 기록을 수정할 상대 회원의 id 입니다.")
    @Operation(summary = "나와 상대 회원의 매칭 기록 상태 수정 API", description = "나와 특정 상대 회원의 매칭 기록 상태를 수정하는 API 입니다.")
    public ApiResponse<String> modifyBothMatching(
        @PathVariable(name = "memberId") Long memberId,
        @RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request,
        @PathVariable(name = "targetMemberId") Long targetMemberId) {

        matchingService.updateBothStatus(request, memberId, targetMemberId);

        return ApiResponse.onSuccess("매칭 상태 변경 성공");
    }

    @PatchMapping("/{memberId}/matching/found/target/{targetMemberId}/{gameMode}")
    @Parameter(name = "targetMemberId", description = "매칭 상대 회원의 id 입니다.")
    @Operation(summary = "매칭 FOUND API", description = "나와 특정 상대 회원의 매칭 기록 상태를 FOUND 상태로 변경하고, 매칭 요청 데이터를 리턴하는 API 입니다.")
    public ApiResponse<MatchingResponse.matchingFoundResponseDTO> matchingFound(
        @PathVariable(name = "memberId") Long memberId,
        @PathVariable(name = "targetMemberId") Long targetMemberId,
        @PathVariable(name = "gameMode") Integer gameMode) {

        return ApiResponse.onSuccess(
            matchingService.foundMatching(memberId, targetMemberId, gameMode));

    }

    @PatchMapping("/{memberId}/success/target/{targetMemberId}/{gameMode}")
    @Parameter(name = "targetMemberId", description = "매칭 상대 회원의 id 입니다.")
    @Operation(summary = "매칭 SUCCESS API", description = "나와 특정 상대 회원의 매칭 기록 상태를 SUCCESS 상태로 변경하고, 채팅방을 시작해 uuid를 리턴하는 API 입니다.")
    public ApiResponse<String> matchingSuccess(
        @PathVariable(name = "memberId") Long memberId,
        @PathVariable(name = "targetMemberId") Long targetMemberId,
        @PathVariable(name = "gameMode") Integer gameMode) {

        matchingService.successMatching(memberId, targetMemberId, gameMode);
        String chatroomUuid = chatCommandService.startChatroomByMatching(memberId, targetMemberId);

        return ApiResponse.onSuccess(chatroomUuid);
    }
}
