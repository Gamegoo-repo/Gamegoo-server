package com.gamegoo.controller.matching;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.service.matching.MatchingService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/matching")
@Slf4j
public class MatchingController {
    private final MatchingService matchingService;

    @PostMapping("/priority")
    @Operation(summary = "우선순위 계산 및 매칭 기록을 저장하는 API 입니다.", description = "API for calculating and recording matching")
    public ApiResponse<MatchingResponse.PriorityMatchingResponseDTO> saveMatching(@RequestBody @Valid MatchingRequest.InitializingMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();

        // 우선순위 계산  
        MatchingResponse.PriorityMatchingResponseDTO priorityMatchingResponseDTO = matchingService.getPriorityLists(request, id);

        // DB에 기록하기
        matchingService.save(request, id);
        return ApiResponse.onSuccess(priorityMatchingResponseDTO);
    }

    @PutMapping("")
    @Operation(summary = "매칭 상태(status)를 수정하는 API입니다.", description = "API for matching status modification")
    public ApiResponse<String> modifyMatching(@RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();
        matchingService.modify(request, id);
        return ApiResponse.onSuccess("매칭 상태 변경에 성공했습니다.");
    }
}
