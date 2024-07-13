package com.gamegoo.domain.board;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "GameStyle2")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameStyle2 extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_style2_id", nullable = false)
    private Long id;

    @Column(name = "style_name", nullable = false, length = 1000)
    private String styleName;

}
