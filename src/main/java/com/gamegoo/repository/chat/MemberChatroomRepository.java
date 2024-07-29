package com.gamegoo.repository.chat;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.chat.MemberChatroom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberChatroomRepository extends JpaRepository<MemberChatroom, Long>,
    MemberChatroomRepositoryCustom {

    @Query("SELECT mc.member FROM MemberChatroom mc WHERE mc.chatroom.id = :chatroomId AND mc.member.id != :memberId")
    Member findTargetMemberByChatroomIdAndMemberId(@Param("chatroomId") Long chatroomId,
        @Param("memberId") Long memberId);

    Optional<MemberChatroom> findByMemberIdAndChatroomId(Long memberId, Long chatroomId);


}
