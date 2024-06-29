package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.GameStyleDTO;
import com.gamegoo.dto.member.PositionDTO;
import com.gamegoo.dto.member.ProfileImageDTO;
import com.gamegoo.service.member.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Slf4j
public class ProfileController {
    private final ProfileService profileService;

    @PutMapping("/gamestyle")
    @Operation(summary = "gamestyle 추가 및 수정 API 입니다.", description = "API for Gamestyle addition and modification ")
    public ApiResponse<Object> addGameStyle(@RequestBody GameStyleDTO gameStyleDTO) {
        profileService.addMemberGameStyles(gameStyleDTO.getGamestyle());
        return ApiResponse.onSuccess("게임 스타일 수정이 완료되었습니다.");
    }

    @PutMapping("/position")
    @Operation(summary = "주/부 포지션 수정 API 입니다.", description = "API for Main/Sub Position Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody PositionDTO positionDTO) {
        int mainP = positionDTO.getMainP();
        int subP = positionDTO.getSubP();
        profileService.modifyPosition(mainP, subP);
        return ApiResponse.onSuccess("포지션 수정이 완료되었습니다. ");
    }

    @PutMapping("/profile_image")
    @Operation(summary = "프로필 이미지 수정 API 입니다.", description = "API for Profile Image Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody ProfileImageDTO profileImageDTO) {
        String profileImage = profileImageDTO.getProfile_image();
        profileService.modifyProfileImage(profileImage);
        return ApiResponse.onSuccess("프로필 이미지 수정이 완료되었습니다.");
    }

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴 API 입니다.", description = "API for  Member")
    public ApiResponse<Object> blindMember() {
        profileService.deleteMember();
        return ApiResponse.onSuccess("탈퇴처리가 완료되었습니다.");

    }

}
