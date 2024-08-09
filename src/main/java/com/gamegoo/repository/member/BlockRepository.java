package com.gamegoo.repository.member;

import com.gamegoo.domain.Block;
import com.gamegoo.domain.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerMemberAndBlockedMember(Member blockerMember, Member blockedMember);

    Optional<Block> findByBlockerMemberAndBlockedMember(Member blockerMember, Member blockedMember);

}
