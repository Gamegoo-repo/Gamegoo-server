package com.gamegoo.repository.member;

import com.gamegoo.domain.Friend;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByFromMemberId(Long memberId);
}
