package com.gamegoo.repository.chat;

import static com.gamegoo.domain.chat.QChat.chat;
import static com.gamegoo.domain.chat.QChatroom.chatroom;
import static com.gamegoo.domain.chat.QMemberChatroom.memberChatroom;

import com.gamegoo.domain.chat.MemberChatroom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MemberChatroomRepositoryCustomImpl implements MemberChatroomRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<MemberChatroom> findActiveMemberChatroomOrderByLastChat(Long memberId) {
        return queryFactory.selectFrom(memberChatroom)
            .join(memberChatroom.chatroom, chatroom)
            .where(
                memberChatroom.member.id.eq(memberId),
                memberChatroom.lastJoinDate.isNotNull()
            )
            .orderBy(new OrderSpecifier<>(
                    Order.DESC,
                    new Coalesce<LocalDateTime>()
                        .add(JPAExpressions.select(chat.createdAt.max())
                            .from(chat)
                            .where(chat.chatroom.eq(chatroom)))
                        .add(chatroom.createdAt)
                )
            )
            .fetch();
    }
}
