package com.gamegoo.domain.board;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.gamestyle.GameStyle;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "BoardGameStyle")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BoardGameStyle extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_game_style_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gamestyle_id", nullable = false)
    private GameStyle gameStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 연관관계 메소드
    public void setBoard(Board board){
        if (this.board != null){
            this.board.getBoardGameStyles().remove(this);
        }
        this.board=board;
        this.board.getBoardGameStyles().add(this);
    }

}
