package com.gamegoo.domain.chat;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.enums.ChatroomType;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Chatroom extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "chatroom_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private ChatroomType chatroomType;

    @Column(columnDefinition = "TEXT")
    private String postUrl;
}
