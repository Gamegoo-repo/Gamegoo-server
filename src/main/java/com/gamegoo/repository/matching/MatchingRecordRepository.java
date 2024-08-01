package com.gamegoo.repository.matching;

import com.gamegoo.domain.MatchingRecord;
import com.gamegoo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRecordRepository extends JpaRepository<MatchingRecord, Long> {
    MatchingRecord findByMember(Member member);
}
