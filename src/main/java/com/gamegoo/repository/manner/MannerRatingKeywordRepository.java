package com.gamegoo.repository.manner;

import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.manner.MannerRatingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MannerRatingKeywordRepository extends JpaRepository<MannerRatingKeyword, Long> {
    List<MannerRatingKeyword> findByMannerRating(MannerRating mannerRating);
}
