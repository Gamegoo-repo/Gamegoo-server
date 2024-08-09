package com.gamegoo.repository.friend;

import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.friend.Friend;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByFromMemberId(Long memberId);

    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member toMember);
}
