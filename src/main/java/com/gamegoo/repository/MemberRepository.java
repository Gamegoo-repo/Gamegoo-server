package com.gamegoo.repository;

import com.gamegoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);

    Boolean existsByEmail(String email);

}
