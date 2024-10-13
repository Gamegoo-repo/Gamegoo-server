package com.gamegoo.converter;

import com.gamegoo.domain.notification.Notification;
import com.gamegoo.dto.notification.NotificationResponse;
import com.gamegoo.dto.notification.NotificationResponse.notificationDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
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

    public static NotificationResponse.pageNotificationListDTO toPageNotificationListDTO(
        Page<Notification> notifications) {
        List<notificationDTO> notificationDTOList = notifications.stream()
            .map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());

        return NotificationResponse.pageNotificationListDTO.builder()
            .notificationDTOList(notificationDTOList)
            .listSize(notificationDTOList.size())
            .totalPage(notifications.getTotalPages())
            .totalElements(notifications.getTotalElements())
            .isFirst(notifications.isFirst())
            .isLast(notifications.isLast())
            .build();
    }

    public static NotificationResponse.notificationDTO toNotificationDTO(
        Notification notification) {

        // notificationType 설정
        Integer notificationType = null;
        Integer originType = notification.getNotificationType().getId().intValue();
        if (originType.equals(7)) {
            notificationType = 3;
        } else if (originType.equals(5) || originType.equals(6)) {
            notificationType = 2;
        } else {
            notificationType = 1;
        }

        // pageUrl 값 생성
        String pageUrl = null;

        if (notification.getNotificationType().getSourceUrl() != null) {

            StringBuilder urlBuilder = new StringBuilder(
                notification.getNotificationType().getSourceUrl());

            if (notification.getSourceMember() != null) { // sourceMember가 존재하는 경우
                if (!notification.getSourceMember().getBlind()) { // 해당 sourceMember가 탈퇴한 회원이 아닌 경우
                    urlBuilder.append(notification.getSourceMember().getId());
                    pageUrl = urlBuilder.toString();

                }
            } else {// sourceMember가 없는 경우: 매너평가 관련 알림
                pageUrl = urlBuilder.toString();

            }

        }

        // content 값 생성
        String content = notification.getContent();

        // sourceMember 닉네임 표시
        if (notification.getSourceMember() != null) {
            if (notification.getSourceMember().getBlind()) { // sourceMember가 탈퇴한 회원인 경우
                content = "(탈퇴한 사용자)" + content;
            } else {
                content = notification.getSourceMember().getGameName() + content;
            }

        }

        return NotificationResponse.notificationDTO.builder()
            .notificationId(notification.getId())
            .notificationType(notificationType)
            .content(content)
            .pageUrl(pageUrl)
            .read(notification.isRead())
            .createdAt(notification.getCreatedAt().withNano(0))
            .build();
    }
}
