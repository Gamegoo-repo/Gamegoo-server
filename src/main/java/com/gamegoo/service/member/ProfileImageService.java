package com.gamegoo.service.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Service
public class ProfileImageService {
    private final MemberRepository memberRepository;
    private final HttpServletResponse response;

    @Autowired
    public ProfileImageService(MemberRepository memberRepository, HttpServletResponse response) {
        this.memberRepository = memberRepository;
        this.response = response;
    }

    public void modifyPosition(String profileImage) throws IOException {
        Long userId = SecurityUtil.getCurrentUserId();
        ErrorStatus errorStatus = null;

        if (profileImage.length() > 30) {
            errorStatus = ErrorStatus.PROFILE_IMAGE_BAD_REQUEST;
        } else {
            Optional<Member> optionalMember = memberRepository.findById(userId);

            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();
                member.setProfileImage(profileImage);
                memberRepository.save(member);
            } else {
                errorStatus = ErrorStatus.MEMBER_NOT_FOUND;
            }
        }


        if (errorStatus != null) {
            ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage(), null);

            response.setStatus(errorStatus.getHttpStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            new ObjectMapper().writeValue(response.getWriter(), apiResponse);

        }
    }
}
