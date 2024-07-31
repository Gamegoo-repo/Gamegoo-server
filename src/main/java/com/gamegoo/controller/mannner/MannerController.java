package com.gamegoo.controller.mannner;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.dto.manner.MannerRequest;
import com.gamegoo.dto.manner.MannerResponse;
import com.gamegoo.service.manner.MannerService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/manner")
@Tag(name = "MannerRating", description = "매너 평가 관련 API")
public class MannerController {

    private final MannerService mannerService;

    @PostMapping("/good")
    @Operation(summary = "매너 평가 등록 API",  description = "매너 평가하기 API 입니다.")
    public ApiResponse<MannerResponse.mannerInsertResponseDTO> mannerInsert(
            @RequestBody MannerRequest.mannerInsertDTO request
        ){
        Long memberId = JWTUtil.getCurrentUserId();

        MannerRating mannerrating = mannerService.insertManner(request, memberId);

        List<Long> mannerRatingKeywordList = mannerrating.getMannerRatingKeywordList().stream()
                .map(mannerRatingKeyword -> mannerRatingKeyword.getMannerKeyword().getId())
                .collect(Collectors.toList());

        MannerResponse.mannerInsertResponseDTO result = MannerResponse.mannerInsertResponseDTO.builder()
                .mannerId(mannerrating.getId())
                .toMemberId(mannerrating.getToMember().getId())
                .mannerRatingKeywordList(mannerRatingKeywordList)
                .build();

        return ApiResponse.onSuccess(result);
    }

}
