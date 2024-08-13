package com.gamegoo.repository.notification;

import com.gamegoo.domain.notification.Notification;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    Slice<Notification> findNotificationsByCursor(Long memberId, Long cursor);
}
