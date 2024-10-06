package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.converter.MemberConverter;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.domain.friend.FriendRequestStatus;
import com.gamegoo.domain.friend.FriendRequests;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.friend.FriendRepository;
import com.gamegoo.repository.friend.FriendRequestsRepository;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberGameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gamegoo.service.manner.MannerService;
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
    private final MemberChatroomRepository memberChatroomRepository;
    private final BoardRepository boardRepository;
    private final MannerService mannerService;
    private final AuthService authService;

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
        System.out.println("GAMESTYLE STARTED ID: "+memberId);

        System.out.println("gameStyleIdList 내용:");
        if (gameStyleIdList != null && !gameStyleIdList.isEmpty()) {
            for (Long id : gameStyleIdList) {
                System.out.println(id);
            }
        } else {
            System.out.println("gameStyleIdList가 비어있거나 null입니다.");
        }

        // 요청으로 온 gameStyleId로 GameStyle 엔티티 리스트를 생성 및 검증
        List<GameStyle> requestGameStyleList = new ArrayList<>();
        if (gameStyleIdList != null && !gameStyleIdList.isEmpty()) {
            requestGameStyleList = gameStyleIdList.stream()
                    .map(gameStyleId -> gameStyleRepository.findById(gameStyleId)
                            .orElseThrow(() -> new MemberHandler(ErrorStatus.GAMESTYLE_NOT_FOUND)))
                    .toList();
        }
        System.out.println("G1 ");
        // 현재 DB에 저장된 MemberGameStyle 목록을 가져옴
        List<MemberGameStyle> currentMemberGameStyleList = new ArrayList<>(member.getMemberGameStyleList());
        System.out.println("G2 : ");
        // 요청된 gameStyleId가 빈 리스트인 경우, 모든 MemberGameStyle을 삭제
        if (requestGameStyleList.isEmpty()) {
            for (MemberGameStyle memberGameStyle : currentMemberGameStyleList) {
                memberGameStyle.removeMember(member); // 양방향 연관관계 제거
                memberGameStyleRepository.delete(memberGameStyle);
            }
            return new ArrayList<>(); // 빈 리스트 반환
        }
        System.out.println("G3 : ");
        // DB에는 존재하나, 요청에는 없는 gameStyle 삭제
        List<MemberGameStyle> toRemove = new ArrayList<>();
        for (MemberGameStyle memberGameStyle : currentMemberGameStyleList) {
            if (!requestGameStyleList.contains(memberGameStyle.getGameStyle())) {
                toRemove.add(memberGameStyle);
            }
        }

        for (MemberGameStyle memberGameStyle : toRemove) {
            memberGameStyle.removeMember(member); // 양방향 연관관계 제거
            memberGameStyleRepository.delete(memberGameStyle);
        }
        System.out.println("G4 : ");

        // 요청에는 있으나, DB에 없는 gameStyle 추가
        List<GameStyle> currentGameStyleList = currentMemberGameStyleList.stream()
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

        // 해당 회원이 속한 모든 채팅방에서 퇴장 처리
        List<MemberChatroom> allActiveMemberChatroom = memberChatroomRepository.findAllActiveMemberChatroom(
            member.getId());
        allActiveMemberChatroom.forEach(memberChatroom -> memberChatroom.updateLastJoinDate(null));

        // 해당 회원이 보낸 모든 친구 요청 취소 처리
        List<FriendRequests> sendFriendRequestsList = friendRequestsRepository.findAllByFromMemberAndStatus(
            member, FriendRequestStatus.PENDING);
        sendFriendRequestsList.forEach(
            friendRequests -> friendRequests.updateStatus(FriendRequestStatus.CANCELLED));

        // 해당 회원이 받은 모든 친구 요청 취소 처리
        List<FriendRequests> receivedFriendRequestsList = friendRequestsRepository.findAllByToMemberAndStatus(
            member, FriendRequestStatus.PENDING);
        receivedFriendRequestsList.forEach(
            friendRequests -> friendRequests.updateStatus(FriendRequestStatus.CANCELLED));

        // 게시판 글 삭제 처리 (deleted = true)
        boardRepository.deleteByMemberId(member.getId());

        // 매너,비매너 평가 기록 삭제 처리
        mannerService.deleteMannerRatingsByMemberId(member.getId());

        // refresh token 삭제하기
        authService.deleteRefreshToken(userId);

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
        Long targetMemberId, Double mannerScoreRank) {
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
            friendRequestMemberId, mannerScoreRank);
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
