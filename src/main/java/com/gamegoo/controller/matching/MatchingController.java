package com.gamegoo.controller.matching;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.matching.MatchingRequest;
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

    @PostMapping("")
    @Operation(summary = "매칭 기록을 DB에 저장하는 API 입니다.", description = "API for recording matching")
    public ApiResponse<String> saveMatching(@RequestBody @Valid MatchingRequest.SaveMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();

        matchingService.save(request, id);
        return ApiResponse.onSuccess("매칭 기록에 성공했습니다.");
    }

    @PutMapping("")
    @Operation(summary = "매칭 상태(status)를 수정하는 API입니다.", description = "API for matching status modification")
    public ApiResponse<String> modifyMatching(@RequestBody @Valid MatchingRequest.ModifyMatchingRequestDTO request) {
        Long id = JWTUtil.getCurrentUserId();
        matchingService.modify(request, id);
        return ApiResponse.onSuccess("매칭 상태 변경에 성공했습니다.");
    }
}
