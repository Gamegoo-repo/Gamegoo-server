package com.gamegoo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GameStyle")
@Getter
@Setter
public class GameStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "gamestyle_id", nullable = false)
    private Long id;

    @Column(name = "style_name", nullable = false, length = 30)
    private String styleName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
