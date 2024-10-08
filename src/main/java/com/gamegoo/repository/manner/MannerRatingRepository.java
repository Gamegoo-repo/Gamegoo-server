package com.gamegoo.repository.manner;

import com.gamegoo.domain.manner.MannerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MannerRatingRepository extends JpaRepository<MannerRating, Long> {
    List<MannerRating> findByToMemberId(Long toMember);
    List<MannerRating> findByFromMemberIdAndToMemberId(Long fromMember, Long toMember);
    List<MannerRating> findByFromMemberId(Long memberId);

    @Query("SELECT COUNT(DISTINCT mr.fromMember.id) FROM MannerRating mr WHERE mr.toMember.id = :memberId")
    Long countDistinctFromMemberByToMemberId(@Param("memberId") Long memberId);

    @Query("DELETE FROM MannerRating mr WHERE mr.fromMember.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
