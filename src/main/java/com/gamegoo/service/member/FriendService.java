package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.FriendHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.domain.friend.FriendRequestStatus;
import com.gamegoo.domain.friend.FriendRequests;
import com.gamegoo.domain.notification.NotificationTypeTitle;
import com.gamegoo.repository.friend.FriendRepository;
import com.gamegoo.repository.friend.FriendRequestsRepository;
import com.gamegoo.service.notification.NotificationService;
import com.gamegoo.util.MemberUtils;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestsRepository friendRequestsRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    /**
     * memberId에 해당하는 회원의 친구 목록 조회
     *
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public List<Friend> getFriends(Long memberId) {
        return friendRepository.findAllByFromMemberId(memberId);
    }


    /**
     * targetMemberId에 해당하는 회원에게 친구 요청 전송
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    public FriendRequests sendFriendRequest(Long memberId, Long targetMemberId) {
        Member member = profileService.findMember(memberId);

        Member targetMember = profileService.findMember(targetMemberId);

        // targetMember로 나 자신을 요청한 경우
        if (member.equals(targetMember)) {
            throw new FriendHandler(ErrorStatus.FRIEND_BAD_REQUEST);
        }

        // 내가 상대방을 차단한 경우
        if (MemberUtils.isBlocked(member, targetMember)) {
            throw new FriendHandler(ErrorStatus.FRIEND_TARGET_IS_BLOCKED);
        }

        // 상대방이 나를 차단한 경우
        if (MemberUtils.isBlocked(targetMember, member)) {
            throw new FriendHandler(ErrorStatus.BLOCKED_BY_FRIEND_TARGET);
        }

        // 서로 이미 친구 관계인 경우
        friendRepository.findByFromMemberAndToMember(member, targetMember)
            .ifPresent(friend -> {
                throw new FriendHandler(ErrorStatus.ALREADY_FRIEND);
            });

        // member -> targetMember로 보낸 친구 요청 중 PENDING 상태인 친구 요청이 존재하는 경우
        friendRequestsRepository.findByFromMemberAndToMemberAndStatus(member, targetMember,
                FriendRequestStatus.PENDING)
            .ifPresent(friendRequest -> {
                throw new FriendHandler(ErrorStatus.MY_PENDING_FRIEND_REQUEST_EXIST);
            });

        // targetMember -> member에게 보낸 친구 요청 중 PENDING 상태인 친구 요청이 존재하는 경우
        friendRequestsRepository.findByFromMemberAndToMemberAndStatus(targetMember, member,
                FriendRequestStatus.PENDING)
            .ifPresent(friendRequests -> {
                throw new FriendHandler(ErrorStatus.TARGET_PENDING_FRIEND_REQUEST_EXIST);
            });

        FriendRequests friendRequests = FriendRequests.builder()
            .status(FriendRequestStatus.PENDING)
            .fromMember(member)
            .toMember(targetMember)
            .build();

        FriendRequests savedFriendRequests = friendRequestsRepository.save(friendRequests);

        // 친구 요청 알림 생성
        // member -> targetMember
        notificationService.createNotification(NotificationTypeTitle.FRIEND_REQUEST_SEND,
            targetMember.getGameName(), null, member);

        // targetMember -> member
        notificationService.createNotification(NotificationTypeTitle.FRIEND_REQUEST_RECEIVED,
            member.getGameName(), member.getId(), targetMember);

        return savedFriendRequests;
    }

    /**
     * targetMember -> member로 요청한 FriendRequest를 ACCEPTED 처리
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    public FriendRequests acceptFriendRequest(Long memberId, Long targetMemberId) {
        Member member = profileService.findMember(memberId);

        Member targetMember = profileService.findMember(targetMemberId);

        // targetMember로 나 자신을 요청한 경우
        if (member.equals(targetMember)) {
            throw new FriendHandler(ErrorStatus.FRIEND_BAD_REQUEST);
        }

        // 수락 대기 상태인 FriendRequest 엔티티 조회
        Optional<FriendRequests> pendingFriendRequest = friendRequestsRepository.findByFromMemberAndToMemberAndStatus(
            targetMember, member, FriendRequestStatus.PENDING);

        // 수락 대기 중인 친구 요청이 존재하지 않는 경우
        if (pendingFriendRequest.isEmpty()) {
            throw new FriendHandler(ErrorStatus.PENDING_FRIEND_REQUEST_NOT_EXIST);
        }

        // FriendRequest 엔티티 상태 변경
        pendingFriendRequest.get().updateStatus(FriendRequestStatus.ACCEPTED);

        // 회원 간 Friend 엔티티 생성 및 저장
        Friend targetMemberFriend = Friend.builder()
            .fromMember(targetMember)
            .toMember(member)
            .isLiked(false)
            .build();

        Friend memberFriend = Friend.builder()
            .fromMember(member)
            .toMember(targetMember)
            .isLiked(false)
            .build();

        friendRepository.save(targetMemberFriend);
        friendRepository.save(memberFriend);

        // targetMember에게 친구 요청 수락 알림 생성
        notificationService.createNotification(NotificationTypeTitle.FRIEND_REQUEST_ACCEPTED,
            member.getGameName(), null, targetMember);

        return pendingFriendRequest.get();
    }

    /**
     * fromMember와 toMember가 서로 친구 관계이면, 친구 관계 끊기
     *
     * @param fromMember
     * @param toMember
     */
    public void removeFriendshipIfPresent(Member fromMember, Member toMember) {
        friendRepository.findByFromMemberAndToMember(fromMember, toMember)
            .ifPresent(friend -> {
                friendRepository.deleteById(friend.getId());
                friendRepository.findByFromMemberAndToMember(toMember, fromMember)
                    .ifPresent(reverseFriend -> friendRepository.deleteById(reverseFriend.getId()));
            });
    }

    /**
     * fromMember -> toMember의 FriendRequest 중 PENDING 상태인 요청을 취소 처리
     *
     * @param fromMember
     * @param toMember
     */
    public void cancelPendingFriendRequests(Member fromMember, Member toMember) {
        friendRequestsRepository.findByFromMemberAndToMemberAndStatus(fromMember, toMember,
                FriendRequestStatus.PENDING)
            .ifPresent(
                friendRequests -> friendRequests.updateStatus(FriendRequestStatus.CANCELLED));
    }
}
