package com.gamegoo.repository.member;

import com.gamegoo.domain.EmailVerifyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerifyRecordRepository extends JpaRepository<EmailVerifyRecord, Long> {
    Optional<EmailVerifyRecord> findByEmailOrderByUpdatedAtDesc(String email);
}
