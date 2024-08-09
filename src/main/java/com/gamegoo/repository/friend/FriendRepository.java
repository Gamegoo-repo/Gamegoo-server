package com.gamegoo.repository.friend;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.friend.Friend;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByFromMemberId(Long memberId);

    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member toMember);

    /**
     * 두 방향의 Friend 관계를 조회하여 리스트로 반환
     *
     * @param member1
     * @param member2
     * @return
     */
    @Query("SELECT f FROM Friend f WHERE (f.fromMember = :member1 AND f.toMember = :member2) OR (f.fromMember = :member2 AND f.toMember = :member1)")
    List<Friend> findBothDirections(@Param("member1") Member member1,
        @Param("member2") Member member2);

}
