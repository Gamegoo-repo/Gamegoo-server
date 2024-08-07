package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.FriendHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.friend.Friend;
import com.gamegoo.domain.friend.FriendRequestStatus;
import com.gamegoo.domain.friend.FriendRequests;
import com.gamegoo.repository.friend.FriendRepository;
import com.gamegoo.repository.friend.FriendRequestsRepository;
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

        if (member.equals(targetMember)) {
            throw new FriendHandler(ErrorStatus.FRIEND_BAD_REQUEST);
        }

        // 내가 상대방을 차단한 경우
        if (MemberUtils.isBocked(member, targetMember)) {
            throw new FriendHandler(ErrorStatus.FRIEND_TARGET_IS_BLOCKED);
        }

        // 상대방이 나를 차단한 경우
        if (MemberUtils.isBocked(targetMember, member)) {
            throw new FriendHandler(ErrorStatus.BLOCKED_BY_FRIEND_TARGET);
        }

        FriendRequests friendRequests = FriendRequests.builder()
            .status(FriendRequestStatus.PENDING)
            .fromMember(member)
            .toMember(targetMember)
            .build();

        return friendRequestsRepository.save(friendRequests);
    }
}
