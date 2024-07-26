package com.gamegoo.repository.member;

import com.gamegoo.domain.gamestyle.GameStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameStyleRepository extends JpaRepository<GameStyle, Long> {
    Optional<GameStyle> findById(Long gameStyleId);
}
