package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.PositionDTO;
import com.gamegoo.service.member.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class PositionController {
    private final PositionService positionService;

    @PutMapping("/position")
    @Operation(summary = "주/부 포지션 수정 API 입니다.", description = "API for Main/Sub Position Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody PositionDTO positionDTO) throws IOException {
        int mainP = positionDTO.getMainP();
        int subP = positionDTO.getSubP();
        positionService.modifyPosition(mainP, subP);
        return ApiResponse.onSuccess(null);
    }
}
