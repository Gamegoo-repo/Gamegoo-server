package com.gamegoo.repository.member;

import com.gamegoo.domain.EmailVerifyRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailVerifyRecordRepository extends JpaRepository<EmailVerifyRecord, Long> {
    @Query("SELECT e FROM EmailVerifyRecord e WHERE e.email = :email ORDER BY e.updatedAt DESC")
    Optional<EmailVerifyRecord> findByEmailOrderByUpdatedAtDesc(String email, PageRequest pageRequest);
}