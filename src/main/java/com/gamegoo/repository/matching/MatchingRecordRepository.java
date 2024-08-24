package com.gamegoo.repository.matching;

import com.gamegoo.domain.matching.MatchingRecord;
import com.gamegoo.domain.matching.MatchingStatus;
import com.gamegoo.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, Long> {
    Optional<MatchingRecord> findFirstByMemberOrderByUpdatedAtDesc(Member member);

    @Query("SELECT m FROM MatchingRecord m WHERE m.createdAt > :createdAt AND m.status = :status AND m.gameMode = :gameMode " +
            "AND m.id IN (SELECT MAX(mr.id) FROM MatchingRecord mr WHERE mr.createdAt > :createdAt AND mr.status = :status AND mr.gameMode = :gameMode GROUP BY mr.member)")
    List<MatchingRecord> findTopByCreatedAtAfterAndStatusAndGameModeGroupByMemberId(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("status") MatchingStatus status,
            @Param("gameMode") Integer gameMode
    );
}
