package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
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

    /**
     * MemberGameStyle 데이터 추가 : 회원에 따른 게임 스타일 정보 저장하기
     *
     * @param gameStyleIdList
     * @param memberId
     * @return
     */
    @Transactional
    public List<MemberGameStyle> addMemberGameStyles(List<Long> gameStyleIdList, Long memberId) {
        // 회원 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));


        // 요청으로 온 gamestyleId로 GameStyle 엔티티 리스트를 생성 및 gamestyleId에 해당하는 gamestyle이 db에 존재하는지 검증
        List<GameStyle> requestGameStyleList = gameStyleIdList.stream()
                .map(gameStyleId -> gameStyleRepository.findById(gameStyleId)
                        .orElseThrow(() -> new MemberHandler(ErrorStatus.GAMESTYLE_NOT_FOUND)))
                .toList();

        // db에는 존재하나, request에는 존재하지 않는 gameStyle을 삭제
        List<MemberGameStyle> toRemove = new ArrayList<>();
        for (MemberGameStyle memberGameStyle : member.getMemberGameStyleList()) {
            if (!requestGameStyleList.contains(memberGameStyle.getGameStyle())) {
                toRemove.add(memberGameStyle);
            }
        }

        for (MemberGameStyle memberGameStyle : toRemove) {
            memberGameStyle.removeMember(member); // 양방향 연관관계 제거
            memberGameStyleRepository.delete(memberGameStyle);
        }

        // request에는 존재하나, db에는 존재하지 않는 gameStyle을 추가
        List<GameStyle> currentGameStyleList = member.getMemberGameStyleList().stream()
                .map(MemberGameStyle::getGameStyle)
                .toList();

        for (GameStyle reqGameStyle : requestGameStyleList) {
            if (!currentGameStyleList.contains(reqGameStyle)) {
                MemberGameStyle memberGameStyle = MemberGameStyle.builder()
                        .gameStyle(reqGameStyle)
                        .build();
                memberGameStyle.setMember(member); // 양방향 연관관계 매핑
                memberGameStyleRepository.save(memberGameStyle);
            }
        }

        return member.getMemberGameStyleList();
    }

    /**
     * 회원 탈퇴 처리
     *
     * @param userId
     */
    @Transactional
    public void deleteMember(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // Blind 처리
        member.deactiveMember();

        memberRepository.save(member);
    }

    /**
     * 메인 포지션, 서브 포지션 수정
     *
     * @param userId
     * @param mainP
     * @param subP
     */
    @Transactional
    public void modifyPosition(Long userId, int mainP, int subP) {
        if (mainP < 0 || subP < 0 || mainP > 5 || subP > 5) {
            throw new MemberHandler(ErrorStatus.POSITION_NOT_FOUND);
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updatePosition(mainP, subP);
        memberRepository.save(member);
    }

    /**
     * 프로필 이미지 수정
     *
     * @param userId
     * @param profileImage
     */
    @Transactional
    public void modifyProfileImage(Long userId, Integer profileImage) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateProfileImage(profileImage);

        memberRepository.save(member);
    }

    /**
     * 회원 정보 조회
     *
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
