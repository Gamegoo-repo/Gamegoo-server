package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberGameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MemberRepository memberRepository;
    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;


    @Transactional
    public void addMemberGameStyles(List<String> gameStyles) {

        Long userId = SecurityUtil.getCurrentUserId();

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));


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

            // 요청에 해당하는 gamestyle 테이블에서 찾기
            GameStyle gameStyle = gameStyleRepository.findByStyleName(styleName)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.GAMESTYLE_NOT_FOUND));

            // 있는지 검사하기
            Optional<MemberGameStyle> existingMemberGameStyle = memberGameStyleRepository.findByMemberAndGameStyle(member, gameStyle);

            // 없을 경우
            if (existingMemberGameStyle.isEmpty()) {
                MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                        .member(member)
                        .gameStyle(gameStyle)
                        .build();
                memberGameStyleRepository.save(memberGameStyle);
            }
        }
    }

    public void deleteMember() {
        Long userId = SecurityUtil.getCurrentUserId();

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // Blind 처리
        member.setBlind(true);
        memberRepository.save(member);
    }

    public void modifyPosition(int mainP, int subP) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 만약 mainP, subP 가 1~5의 값이 아닐 경우
        if (mainP <= 0 || subP <= 0 || mainP > 5 || subP > 5) {
            throw new MemberHandler(ErrorStatus.POSITION_NOT_FOUND);
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.setMainPosition(mainP);
        member.setSubPosition(subP);
        memberRepository.save(member);
    }

    public void modifyProfileImage(String profileImage) {
        Long userId = SecurityUtil.getCurrentUserId();

        if (profileImage.length() > 30) {
            throw new MemberHandler(ErrorStatus.PROFILE_IMAGE_BAD_REQUEST);
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.setProfileImage(profileImage);
        memberRepository.save(member);


    }
}
