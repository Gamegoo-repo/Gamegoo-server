package com.gamegoo.repository.notification;

import com.gamegoo.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
    NotificationRepositoryCustom {

}
