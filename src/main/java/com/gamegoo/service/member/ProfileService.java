package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.domain.friend.FriendRequestStatus;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.friend.FriendRepository;
import com.gamegoo.repository.friend.FriendRequestsRepository;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberGameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final GameStyleRepository gameStyleRepository;
    private final MemberGameStyleRepository memberGameStyleRepository;

    private final FriendRepository friendRepository;
    private final FriendRequestsRepository friendRequestsRepository;

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

    @Transactional(readOnly = true)
    public Member findSystemMember() {
        return memberRepository.findById(0L)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    /**
     * 특정 회원의 유저프로필 조회, 차단 여부, 친구 여부, 친구 요청 상태 포함
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    @Transactional(readOnly = true)
    public MemberResponse.memberProfileDTO getTargetMemberProfile(Long memberId,
        Long targetMemberId) {
        Member member = findMember(memberId);
        Member targetMember = findMember(targetMemberId);

        List<Friend> friendList = friendRepository.findBothDirections(member, targetMember);
        boolean isFriend = (friendList.size() == 2);

        Long friendRequestMemberId = friendRequestsRepository.findByFromMemberAndToMemberAndStatus(
                member, targetMember,
                FriendRequestStatus.PENDING)
            .map(friendRequests -> member.getId())
            .or(() -> friendRequestsRepository.findByFromMemberAndToMemberAndStatus(targetMember,
                    member, FriendRequestStatus.PENDING)
                .map(friendRequests -> targetMember.getId()))
            .orElse(null); // 친구 요청이 없는 경우 null을 리턴

        return MemberConverter.toMemberProfileDTO(member, targetMember,
            isFriend,
            friendRequestMemberId);
    }

    /**
     * 해당 회원의 gameStyle string list 반환
     *
     * @param member
     * @return
     */
    @Transactional(readOnly = true)
    public List<String> getGameStyleList(Member member) {
        return member.getMemberGameStyleList().stream()
            .map(memberGameStyle -> memberGameStyle.getGameStyle().getStyleName()).collect(
                Collectors.toList());
    }
}
