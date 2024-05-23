package com.gamegoo.domain.notification;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Boolean isRead;
}
