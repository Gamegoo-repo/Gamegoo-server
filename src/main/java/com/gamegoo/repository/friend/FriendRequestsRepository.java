package com.gamegoo.repository.friend;

import com.gamegoo.domain.friend.FriendRequestStatus;
import com.gamegoo.domain.friend.FriendRequests;
import com.gamegoo.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestsRepository extends JpaRepository<FriendRequests, Long> {

    Optional<FriendRequests> findByFromMemberAndToMemberAndStatus(Member fromMember,
        Member toMember, FriendRequestStatus status);

    List<FriendRequests> findAllByFromMemberAndStatus(Member fromMember,
        FriendRequestStatus status);

    List<FriendRequests> findAllByToMemberAndStatus(Member toMember,
        FriendRequestStatus status);
}
