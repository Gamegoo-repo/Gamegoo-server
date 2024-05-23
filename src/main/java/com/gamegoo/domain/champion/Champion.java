package com.gamegoo.domain.champion;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Champion")
@Getter
@Setter
public class Champion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "champion_id", nullable = false)
    private Long id;

    @Column(name = "champion_name", nullable = false, length = 30)
    private String champion_name;


}
