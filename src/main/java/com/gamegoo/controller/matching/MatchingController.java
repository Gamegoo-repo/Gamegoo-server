package com.gamegoo.controller.matching;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MatchingHandler;
import com.gamegoo.domain.matching.MatchingType;
import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.service.matching.MatchingService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/matching")
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/priority")
    @Operation(summary = "우선순위 계산 및 매칭 기록을 저장하는 API 입니다.", description = "API for calculating and recording matching")
    public ApiResponse<MatchingResponse.PriorityMatchingResponseDTO> saveMatching(
        @RequestBody @Valid MatchingRequest.InitializingMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();

        // 매칭 타입 유효성 검사
        try {
            MatchingType.valueOf(request.getMatchingType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MatchingHandler(ErrorStatus.MATHCING_TYPE_BAD_REQUEST);
        }

        // 우선순위 계산
        MatchingResponse.PriorityMatchingResponseDTO priorityMatchingResponseDTO = matchingService.getPriorityLists(
            request, id);

        // DB에 기록하기
        matchingService.save(request, id);
        return ApiResponse.onSuccess(priorityMatchingResponseDTO);
    }

    @PatchMapping("/status")
    @Operation(summary = "나의 매칭 기록 상태(status)를 수정하는 API입니다.", description = "API for matching status modification")
    public ApiResponse<String> modifyMatching(
        @RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();

        matchingService.updateMyStatus(request, id);
        return ApiResponse.onSuccess("매칭 상태 변경에 성공했습니다.");
    }

    @PatchMapping("/status/target/{targetMemberId}")
    @Parameter(name = "targetMemberId", description = "매칭 기록을 수정할 상대 회원의 id 입니다.")
    @Operation(summary = "나와 상대 회원의 매칭 기록 상태 수정 API", description = "나와 특정 상대 회원의 매칭 기록 상태를 수정하는 API 입니다.")
    public ApiResponse<String> modifyBothMatching(
        @RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request,
        @PathVariable(name = "targetMemberId") Long targetMemberId) {
        Long memberId = JWTUtil.getCurrentUserId();

        matchingService.updateBothStatus(request, memberId, targetMemberId);

        return ApiResponse.onSuccess("매칭 상태 변경 성공");
    }
}
