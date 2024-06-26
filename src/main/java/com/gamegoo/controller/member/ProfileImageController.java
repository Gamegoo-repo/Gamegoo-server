package com.gamegoo.controller.member;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.dto.member.ProfileImageDTO;
import com.gamegoo.service.member.ProfileImageService;
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
public class ProfileImageController {
    private final ProfileImageService profileImageService;

    @PutMapping("/profile_image")
    @Operation(summary = "프로필 이미지 수정 API 입니다.", description = "API for Profile Image Modification")
    public ApiResponse<Object> modifyPosition(@RequestBody ProfileImageDTO profileImageDTO) throws IOException {
        String profileImage = profileImageDTO.getProfile_image();
        profileImageService.modifyPosition(profileImage);
        return ApiResponse.onSuccess(null);
    }

}
