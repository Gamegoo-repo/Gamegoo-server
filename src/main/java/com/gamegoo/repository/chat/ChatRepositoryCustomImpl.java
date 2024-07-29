package com.gamegoo.repository.chat;

import static com.gamegoo.domain.chat.QChat.chat;
import static com.gamegoo.domain.chat.QMemberChatroom.memberChatroom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {

    private final static int PAGE_SIZE = 20;

    private final JPAQueryFactory queryFactory;

    @Override
    public Integer countUnreadChats(Long chatroomId, Long memberChatroomId) {

        Long countResult = queryFactory.select(chat.count())
            .from(chat)
            .where(
                chat.chatroom.id.eq(chatroomId),
                createdAtGreaterThanSubQuery(memberChatroomId)
            )
            .fetchOne();

        return countResult != null ? countResult.intValue() : null;
    }

    //--- BooleanExpression ---//
    private BooleanExpression createdAtGreaterThanSubQuery(Long memberChatroomId) {
        return chat.createdAt.gt(
            JPAExpressions.select(memberChatroom.lastViewDate)
                .from(memberChatroom)
                .where(memberChatroom.id.eq(memberChatroomId))
        );
    }
}
