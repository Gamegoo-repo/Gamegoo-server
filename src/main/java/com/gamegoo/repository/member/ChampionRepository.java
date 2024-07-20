package com.gamegoo.repository.member;

import com.gamegoo.domain.champion.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChampionRepository extends JpaRepository<Champion, Long> {
    Optional<Champion> findById(Long id);
}
