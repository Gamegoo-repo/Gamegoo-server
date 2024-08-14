package com.gamegoo.repository.matching;

import com.gamegoo.domain.matching.MatchingRecord;
import com.gamegoo.domain.matching.MatchingStatus;
import com.gamegoo.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, Long> {
    Optional<MatchingRecord> findFirstByMemberOrderByUpdatedAtDesc(Member member);

    List<MatchingRecord> findByCreatedAtBeforeAndStatusAndGameMode(LocalDateTime createdAt, MatchingStatus status, Integer gameMode);
}
