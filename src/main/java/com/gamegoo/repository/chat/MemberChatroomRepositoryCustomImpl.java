package com.gamegoo.repository.chat;

import static com.gamegoo.domain.chat.QChat.chat;
import static com.gamegoo.domain.chat.QChatroom.chatroom;
import static com.gamegoo.domain.chat.QMemberChatroom.memberChatroom;

import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.chat.QChatroom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@Slf4j
@RequiredArgsConstructor
public class MemberChatroomRepositoryCustomImpl implements MemberChatroomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 해당 회원의 모든 입장 상태인 채팅방 조회, 해당 채팅방의 마지막 채팅 시각을 기준으로 내림차순 정렬하고, 페이징 포함
     *
     * @param memberId
     * @param cursor
     * @param pageSize
     * @return
     */
    @Override
    public Slice<MemberChatroom> findActiveMemberChatroomByCursorOrderByLastChat(Long memberId,
        Long cursor, Integer pageSize) {

        List<MemberChatroom> result = queryFactory.selectFrom(memberChatroom)
            .join(memberChatroom.chatroom, chatroom)
            .where(
                memberChatroom.member.id.eq(memberId),
                memberChatroom.lastJoinDate.isNotNull(),
                lastMsgLessThanCursor(cursor, chatroom)
            )
            .orderBy(new OrderSpecifier<>(
                    Order.DESC,
                    new Coalesce<LocalDateTime>()
                        .add(JPAExpressions.select(chat.createdAt.max())
                            .from(chat)
                            .where(
                                chat.chatroom.eq(chatroom),
                                chat.createdAt.goe(memberChatroom.lastJoinDate)))
                        .add(memberChatroom.lastJoinDate) // 해당 채팅방에 대화 내역이 없는 경우, lastJoinDate를 기준으로 정렬
                )
            )
            .limit(pageSize + 1) // 다음 페이지가 있는지 확인하기 위해 +1
            .fetch();

        boolean hasNext = false;
        if (result.size() > pageSize) {
            result.remove(pageSize.intValue());
            hasNext = true;
        }

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        return new SliceImpl<>(result, pageRequest, hasNext);
    }

    /**
     * 해당 회원의 모든 입장 상태인 채팅방 조회, 정렬 및 페이징 미포함
     *
     * @param memberId
     * @return
     */
    @Override
    public List<MemberChatroom> findAllActiveMemberChatroom(Long memberId) {
        return queryFactory.selectFrom(memberChatroom)
            .join(memberChatroom.chatroom, chatroom)
            .where(
                memberChatroom.member.id.eq(memberId),
                memberChatroom.lastJoinDate.isNotNull()
            )
            .fetch();
    }

    //--- BooleanExpression ---//
    private BooleanExpression lastMsgLessThanCursor(Long cursor, QChatroom chatroom) {
        if (cursor == null) {
            return null; // null 처리
        }

        return JPAExpressions.select(chat.timestamp.max())
            .from(chat)
            .where(
                chat.chatroom.eq(chatroom)
            ).lt(cursor);
    }
}
