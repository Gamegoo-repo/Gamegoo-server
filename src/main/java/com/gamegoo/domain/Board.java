package com.gamegoo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Board")
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(name = "voice", nullable = false)
    private Boolean voice;

    @Column(name = "content", nullable = false, length = 3000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


}

