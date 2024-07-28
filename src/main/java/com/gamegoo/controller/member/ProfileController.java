package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.dto.member.MemberRequest;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/gamestyle")
    @Operation(summary = "gamestyle 추가 및 수정 API 입니다.", description = "API for Gamestyle addition and modification ")
    public ApiResponse<List<MemberResponse.GameStyleResponseDTO>> addGameStyle(
        @RequestBody MemberRequest.GameStyleRequestDTO gameStyleRequestDTO) {
        Long memberId = JWTUtil.getCurrentUserId();
        List<MemberGameStyle> memberGameStyles = profileService.addMemberGameStyles(
            gameStyleRequestDTO, memberId);

        List<MemberResponse.GameStyleResponseDTO> dtoList = memberGameStyles.stream()
            .map(memberGameStyle -> MemberResponse.GameStyleResponseDTO.builder()
                .gameStyleId(memberGameStyle.getGameStyle().getId())
                .gameStyleName(memberGameStyle.getGameStyle().getStyleName())
                .build()).collect(Collectors.toList());

        return ApiResponse.onSuccess(dtoList);
    }

    @PutMapping("/position")
    @Operation(summary = "주/부 포지션 수정 API 입니다.", description = "API for Main/Sub Position Modification")
    public ApiResponse<String> modifyPosition(
        @RequestBody @Valid MemberRequest.PositionRequestDTO positionRequestDTO) {
        Long userId = JWTUtil.getCurrentUserId();
        int mainP = positionRequestDTO.getMainP();
        int subP = positionRequestDTO.getSubP();

        profileService.modifyPosition(userId, mainP, subP);

        return ApiResponse.onSuccess("포지션 수정이 완료되었습니다. ");
    }

    @PutMapping("/profile_image")
    @Operation(summary = "프로필 이미지 수정 API 입니다.", description = "API for Profile Image Modification")
    public ApiResponse<String> modifyPosition(
        @RequestBody MemberRequest.ProfileImageRequestDTO profileImageDTO) {
        Long userId = JWTUtil.getCurrentUserId();
        String profileImage = profileImageDTO.getProfileImage();

        profileService.modifyProfileImage(userId, profileImage);

        return ApiResponse.onSuccess("프로필 이미지 수정이 완료되었습니다.");
    }

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴 API 입니다.", description = "API for  Member")
    public ApiResponse<String> blindMember() {
        Long userId = JWTUtil.getCurrentUserId();

        profileService.deleteMember(userId);
        return ApiResponse.onSuccess("탈퇴처리가 완료되었습니다.");

    }

    @Operation(summary = "마이페이지 조회 API", description = "나 -> 나 조회하는 마이페이지 API 입니다.")
    @GetMapping("/profile")
    public ApiResponse<MemberResponse.myProfileMemberDTO> getBlockList() {
        Long memberId = JWTUtil.getCurrentUserId();

        Member myProfile = profileService.findMember(memberId);

        return ApiResponse.onSuccess(MemberConverter.toMyProfileDTO(myProfile));
    }

}
