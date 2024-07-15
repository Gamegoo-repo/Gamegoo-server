package com.gamegoo.repository.member;

import com.gamegoo.domain.champion.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChampionRepository extends JpaRepository<Champion, Long> {
}
