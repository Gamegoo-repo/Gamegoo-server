package com.gamegoo.repository.manner;

import com.gamegoo.domain.manner.MannerRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MannerRatingRepository extends JpaRepository<MannerRating, Long> {
    List<MannerRating> findByFromMemberIdAndToMemberId(Long fromMember, Long toMember);
}
