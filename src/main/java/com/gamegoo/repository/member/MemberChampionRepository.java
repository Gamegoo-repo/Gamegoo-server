package com.gamegoo.repository.member;

import com.gamegoo.domain.champion.MemberChampion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberChampionRepository extends JpaRepository<MemberChampion, Long> {
}
