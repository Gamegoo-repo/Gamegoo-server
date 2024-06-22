package com.gamegoo.service.member;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberGameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MemberGameStyleService {

    private final MemberRepository memberRepository;
    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;
    private final HttpServletResponse response;

    @Autowired
    public MemberGameStyleService(MemberRepository memberRepository, GameStyleRepository gameStyleRepository, MemberGameStyleRepository memberGameStyleRepository, HttpServletResponse response) {
        this.memberRepository = memberRepository;
        this.gameStyleRepository = gameStyleRepository;
        this.memberGameStyleRepository = memberGameStyleRepository;
        this.response = response;
    }

    @Transactional
    public void addMemberGameStyles(List<String> gameStyles) throws IOException {
        Long userId = SecurityUtil.getCurrentUserId();
        ErrorStatus errorStatus = null;
        
        Optional<Member> member = memberRepository.findById(userId);

        if (member.isEmpty()) {
            errorStatus = ErrorStatus.MEMBER_NOT_FOUND;
        }

        // 요청된 game style 목록을 Set으로 변환
        Set<String> requestedGameStyles = new HashSet<>(gameStyles);

        // 현재 사용자의 모든 MemberGameStyle을 가져옴
        List<MemberGameStyle> existingMemberGameStyles = memberGameStyleRepository.findByMember(member);

        // 요청되지 않은 game style을 삭제
        existingMemberGameStyles.stream()
                .filter(mgs -> !requestedGameStyles.contains(mgs.getGameStyle().getStyleName()))
                .forEach(memberGameStyleRepository::delete);

        // 요청 중에 repository에 없는 항목을 추가
        for (String styleName : gameStyles) {
            Optional<GameStyle> gameStyle = gameStyleRepository.findByStyleName(styleName);

            if (gameStyle.isEmpty()) {
                errorStatus = ErrorStatus.GAMESTYLE_NOT_FOUND;
            }

            // errorStatus가 null이 아닌 경우 error Response
            if (errorStatus != null) {
                ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage(), null);

                response.setStatus(errorStatus.getHttpStatus().value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                new ObjectMapper().writeValue(response.getWriter(), apiResponse);
            }

            Optional<MemberGameStyle> existingMemberGameStyle = memberGameStyleRepository.findByMemberAndGameStyle(member, gameStyle);

            // 없을 경우
            if (existingMemberGameStyle.isEmpty()) {
                if (member.isPresent() && gameStyle.isPresent()) {
                    MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                            .member(member.get())
                            .gameStyle(gameStyle.get())
                            .build();
                    memberGameStyleRepository.save(memberGameStyle);
                }

            }
        }
    }
}