package com.gamegoo.domain.gamestyle;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "GameStyle")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameStyle extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "gamestyle_id", nullable = false)
    private Long id;

    @Column(name = "style_name", nullable = false, length = 100)
    private String styleName;

}
