package com.gamegoo.repository.member;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberGameStyleRepository extends JpaRepository<MemberGameStyle, Long> {
    Optional<MemberGameStyle> findByMemberAndGameStyle(Optional<Member> member, Optional<GameStyle> gameStyle);

    List<MemberGameStyle> findByMember(Optional<Member> member);
}
