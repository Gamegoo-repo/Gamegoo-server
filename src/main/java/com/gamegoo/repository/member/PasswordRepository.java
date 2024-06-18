package com.gamegoo.repository.member;

import com.gamegoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
}
