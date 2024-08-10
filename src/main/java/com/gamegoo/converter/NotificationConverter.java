package com.gamegoo.converter;

import com.gamegoo.domain.notification.Notification;
import com.gamegoo.dto.notification.NotificationResponse;
import com.gamegoo.dto.notification.NotificationResponse.notificationDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;

public class NotificationConverter {

    public static NotificationResponse.cursorNotificationListDTO toCursorNotificationListDTO(
        Slice<Notification> notifications) {
        List<notificationDTO> notificationDTOList = notifications.stream()
            .map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());

        return NotificationResponse.cursorNotificationListDTO.builder()
            .notificationDTOList(notificationDTOList)
            .list_size(notificationDTOList.size())
            .has_next(notifications.hasNext())
            .next_cursor(notifications.hasNext() ? notifications.getContent().get(9).getId()
                : null) // next cursor를 현재 notificationList의 가장 마지막 요소의 id로 주기
            .build();
    }

    public static NotificationResponse.notificationDTO toNotificationDTO(
        Notification notification) {

        String pageUrl = null;

        if (notification.getNotificationType().getSourceUrl() != null) {
            StringBuilder urlBuilder = new StringBuilder(
                notification.getNotificationType().getSourceUrl());

            if (notification.getSourceId() != null) {
                urlBuilder.append(notification.getSourceId());
            }

            pageUrl = urlBuilder.toString();
        }
        
        return NotificationResponse.notificationDTO.builder()
            .notificationId(notification.getId())
            .notificationType(notification.getNotificationType().getId().intValue())
            .content(notification.getContent())
            .pageUrl(pageUrl)
            .read(notification.isRead())
            .createdAt(notification.getCreatedAt().withNano(0))
            .build();
    }
}
