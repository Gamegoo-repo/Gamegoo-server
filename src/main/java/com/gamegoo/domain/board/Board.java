package com.gamegoo.domain.board;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Board")
@Getter
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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardGameStyle> boardGameStyles = new ArrayList<>();

    // 연관관계 메소드
    public void setMember(Member member){
        if (this.member != null){
            this.member.getBoardList().remove(this);
        }
        this.member = member;
        if (member != null) {
            this.member.getBoardList().add(this);
        }
    }

    public void updateBoard(Integer mode, Integer mainPosition, Integer subPosition, Integer wantPosition, Boolean voice, String content) {
        this.mode = mode;
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        this.wantPosition = wantPosition;
        this.voice = voice;
        this.content = content;
    }

    public void addBoardGameStyle(BoardGameStyle boardGameStyle) {
        this.boardGameStyles.add(boardGameStyle);
        boardGameStyle.setBoard(this);
    }

    public void removeBoardGameStyle(BoardGameStyle boardGameStyle) {
        this.boardGameStyles.remove(boardGameStyle);
        boardGameStyle.setBoard(null);
    }
}
