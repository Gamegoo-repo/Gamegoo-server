package com.gamegoo.repository.member;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGameStyleRepository extends JpaRepository<MemberGameStyle, Long> {
    Optional<MemberGameStyle> findByMemberAndGameStyle(Member member, GameStyle gameStyle);

    List<MemberGameStyle> findByMember(Member member);

    List<MemberGameStyle> findByMemberAndGameStyleNotIn(Member member, List<GameStyle> gameStyles);

}
