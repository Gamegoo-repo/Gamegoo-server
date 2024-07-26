package com.gamegoo.domain.champion;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Champion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Champion {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    // Champion 생성자
    @Builder
    public Champion(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
