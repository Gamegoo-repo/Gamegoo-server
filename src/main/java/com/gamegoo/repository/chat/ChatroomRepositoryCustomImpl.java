package com.gamegoo.repository.chat;

import static com.gamegoo.domain.chat.QChatroom.chatroom;
import static com.gamegoo.domain.chat.QMemberChatroom.memberChatroom;

import com.gamegoo.domain.chat.Chatroom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ChatroomRepositoryCustomImpl implements ChatroomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * memberId1, memberId2에 해당하는 chatroom 엔티티를 반환
     *
     * @param memberId1
     * @param memberId2
     * @return
     */
    @Override
    public Optional<Chatroom> findChatroomByMemberIds(Long memberId1, Long memberId2) {
        Chatroom chatroomEntity = queryFactory
            .select(chatroom)
            .from(memberChatroom)
            .join(chatroom).on(memberChatroom.chatroom.id.eq(chatroom.id))
            .where(memberChatroom.member.id.in(memberId1, memberId2))
            .groupBy(memberChatroom.chatroom.id)
            .having(memberChatroom.member.id.count().eq(2L))
            .fetchFirst();

        return Optional.ofNullable(chatroomEntity);
    }
}
