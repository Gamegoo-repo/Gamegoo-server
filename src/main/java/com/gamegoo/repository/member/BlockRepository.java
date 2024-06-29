package com.gamegoo.repository.member;

import com.gamegoo.domain.Block;
import com.gamegoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerMemberAndBlockedMember(Member blockerMember, Member blockedMember);

}
