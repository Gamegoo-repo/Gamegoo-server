package com.gamegoo.repository.matching;

import com.gamegoo.domain.MatchingRecord;
import com.gamegoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, Long> {
    Optional<MatchingRecord> findFirstByMemberOrderByUpdatedAtDesc(Member member);
}
