package com.gamegoo.domain.chat;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.enums.ChatroomStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberChatroom extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_chatroom_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private ChatroomStatus chatroomStatus;

    private LocalDateTime lastViewDateTime;
}
