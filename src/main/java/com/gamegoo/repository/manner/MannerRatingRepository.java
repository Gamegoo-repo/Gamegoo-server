package com.gamegoo.repository.manner;

import com.gamegoo.domain.manner.MannerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MannerRatingRepository extends JpaRepository<MannerRating, Long> {
    List<MannerRating> findByToMemberId(Long toMember);
    List<MannerRating> findByFromMemberIdAndToMemberId(Long fromMember, Long toMember);

}
