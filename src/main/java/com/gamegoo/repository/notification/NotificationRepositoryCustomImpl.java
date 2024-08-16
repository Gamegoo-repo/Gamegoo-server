package com.gamegoo.repository.notification;

import static com.gamegoo.domain.notification.QNotification.notification;

import com.gamegoo.domain.notification.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@Slf4j
@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final static int CURSOR_PAGE_SIZE = 10;

    private final JPAQueryFactory queryFactory;

    /**
     * 알림 내역 조회, 커서 기반 페이징 포함
     *
     * @param memberId
     * @param type
     * @param cursor
     * @return
     */
    @Override
    public Slice<Notification> findNotificationsByCursor(Long memberId, Long cursor) {

        List<Notification> result = queryFactory.selectFrom(notification)
            .where(
                notification.member.id.eq(memberId),
                idBefore(cursor)
            )
            .orderBy(notification.createdAt.desc())
            .limit(CURSOR_PAGE_SIZE + 1) // 다음 페이지가 있는지 확인하기 위해 +1
            .fetch();

        boolean hasNext = false;
        if (result.size() > CURSOR_PAGE_SIZE) {
            result.remove(CURSOR_PAGE_SIZE);
            hasNext = true;
        }

        PageRequest pageRequest = PageRequest.of(0, CURSOR_PAGE_SIZE);

        return new SliceImpl<>(result, pageRequest, hasNext);
    }

    //--- BooleanExpression ---//

    private BooleanExpression idBefore(Long cursor) {
        return cursor != null ? notification.id.lt(cursor) : null;
    }
}
