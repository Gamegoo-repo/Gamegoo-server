package com.gamegoo.dto.notification;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NotificationResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class cursorNotificationListDTO {

        List<notificationDTO> notificationDTOList;
        Integer list_size;
        Boolean has_next;
        Long next_cursor;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class pageNotificationListDTO {

        List<notificationDTO> notificationDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class notificationDTO {

        Long notificationId;
        int notificationType;
        String content;
        String pageUrl;
        Boolean read;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class notificationReadDTO {

        Long notificationId;
        String message;
    }

}
