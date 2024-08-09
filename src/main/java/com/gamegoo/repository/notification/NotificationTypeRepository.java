package com.gamegoo.repository.notification;

import com.gamegoo.domain.notification.NotificationType;
import com.gamegoo.domain.notification.NotificationTypeTitle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {

    Optional<NotificationType> findNotificationTypeByTitle(NotificationTypeTitle title);

}
