package com.gamegoo.service.member;


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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MemberGameStyleService {

    private final MemberRepository memberRepository;
    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;

    @Autowired
    public MemberGameStyleService(MemberRepository memberRepository, GameStyleRepository gameStyleRepository, MemberGameStyleRepository memberGameStyleRepository) {
        this.memberRepository = memberRepository;
        this.gameStyleRepository = gameStyleRepository;
        this.memberGameStyleRepository = memberGameStyleRepository;
    }

    @Transactional
    public void addMemberGameStyles(List<String> gameStyles) {
        Long userId = SecurityUtil.getCurrentUserId();
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id를 가진 사용자가 없습니다. id : " + userId));

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
            GameStyle gameStyle = gameStyleRepository.findByStyleName(styleName)
                    .orElseThrow(() -> new IllegalArgumentException("잘못된 게임 스타일입니다. gamestyle : " + styleName));

            Optional<MemberGameStyle> existingMemberGameStyle = memberGameStyleRepository.findByMemberAndGameStyle(member, gameStyle);
            if (existingMemberGameStyle.isEmpty()) {
                MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                        .member(member)
                        .gameStyle(gameStyle)
                        .build();
                memberGameStyleRepository.save(memberGameStyle);
            }
        }
    }
}
