package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.dto.member.GameStyleDTO;
import com.gamegoo.dto.member.PositionDTO;
import com.gamegoo.dto.member.ProfileImageDTO;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class ProfileController {
    private final ProfileService profileService;

    @PutMapping("/gamestyle")
    @Operation(summary = "gamestyle 추가 및 수정 API 입니다.", description = "API for Gamestyle addition and modification ")
    public ApiResponse<Object> addGameStyle(@RequestBody GameStyleDTO gameStyleDTO) throws IOException {
        profileService.addMemberGameStyles(gameStyleDTO.getGamestyle());
        return ApiResponse.onSuccess(null);
    }

    @PutMapping("/position")
    @Operation(summary = "주/부 포지션 수정 API 입니다.", description = "API for Main/Sub Position Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody PositionDTO positionDTO) throws IOException {
        int mainP = positionDTO.getMainP();
        int subP = positionDTO.getSubP();
        profileService.modifyPosition(mainP, subP);
        return ApiResponse.onSuccess(null);
    }

    @PutMapping("/profile_image")
    @Operation(summary = "프로필 이미지 수정 API 입니다.", description = "API for Profile Image Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody ProfileImageDTO profileImageDTO) throws IOException {
        String profileImage = profileImageDTO.getProfile_image();
        profileService.modifyProfileImage(profileImage);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴 API 입니다.", description = "API for  Member")
    public ApiResponse<Object> blindMember() {
        Long userId = SecurityUtil.getCurrentUserId(); //헤더에 있는 jwt 토큰에서 id를 가져오는 코드
        try {
            profileService.deleteMember(userId);
            return ApiResponse.onSuccess(null);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);
        }
    }

}
