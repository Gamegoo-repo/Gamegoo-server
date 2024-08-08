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
