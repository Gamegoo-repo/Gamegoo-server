package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.dto.member.MemberRequest;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberGameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MemberRepository memberRepository;
    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;


    @Transactional
    public List<MemberGameStyle> addMemberGameStyles(MemberRequest.GameStyleRequestDTO request, Long memberId) {
        // 회원 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 요청으로 온 gamestyleId로 GameStyle 엔티티 리스트를 생성 및 gamestyleId에 해당하는 gamestyle이 db에 존재하는지 검증
        List<GameStyle> requestGameStyleList = request.getGameStyleIdList().stream()
                .map(gameStyleId -> gameStyleRepository.findById(gameStyleId)
                        .orElseThrow(() -> new MemberHandler(ErrorStatus.GAMESTYLE_NOT_FOUND)))
                .toList();


        // db에는 존재하나, request에는 존재하지 않는 gameStyle을 삭제
        member.getMemberGameStyleList().stream()
                .filter(memberGameStyle -> !requestGameStyleList.contains(memberGameStyle.getGameStyle()))
                .forEach(memberGameStyle -> {
                    memberGameStyle.removeMember(member); // 양방향 연관관계 제거
                    memberGameStyleRepository.delete(memberGameStyle);
                });

        // request에는 존재하나, db에는 존재하지 않는 gameStyle을 추가
        // 현재 member가 가지고 있는 GameStyleList 추출
        List<GameStyle> currentGameStyleList = new ArrayList<>();
        for (MemberGameStyle gameStyle : member.getMemberGameStyleList()) {
            GameStyle style = gameStyle.getGameStyle();
            currentGameStyleList.add(style);
        }

        // memberGameStyle 엔티티 생성 및 연관관계 매핑, db 저장
        requestGameStyleList.stream()
                .filter(reqGameStyle -> !currentGameStyleList.contains(reqGameStyle))
                .forEach(reqGameStyle -> {
                    MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                            .gameStyle(reqGameStyle)
                            .build();
                    memberGameStyle.setMember(member); // 양방향 연관관계 매핑
                    memberGameStyleRepository.save(memberGameStyle);
                });

        return member.getMemberGameStyleList();
    }

    public void deleteMember(Long userId) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // Blind 처리
        member.setBlind(true);
        memberRepository.save(member);
    }

    public void modifyPosition(Long userId, int mainP, int subP) {

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

    public void modifyProfileImage(Long userId, String profileImage) {

        if (profileImage.length() > 30) {
            throw new MemberHandler(ErrorStatus.PROFILE_IMAGE_BAD_REQUEST);
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.setProfileImage(profileImage);
        memberRepository.save(member);


    }
}
