package com.gamegoo.repository.notification;

import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
    NotificationRepositoryCustom {

    Page<Notification> findNotificationsByMember(Member member, Pageable pageable);

}
