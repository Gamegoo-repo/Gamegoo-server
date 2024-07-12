package com.gamegoo.domain;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "Board")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Board extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false)
    private Long id;

    @Column(name = "mode", nullable = false)
    private Integer mode;

    @Column(name = "main_position", nullable = false)
    private Integer mainPosition;

    @Column(name = "sub_position", nullable = false)
    private Integer subPosition;

    @Column(name = "want_position", nullable = false)
    private Integer wantPosition;

    @Column(name = "voice")
    private Boolean voice = false;

    @Column(name = "content", length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}

