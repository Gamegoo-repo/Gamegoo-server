package com.gamegoo.domain.gamestyle;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GameStyle")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameStyle extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gamestyle_id", nullable = false)
    private Long id;

    @Column(name = "style_name", nullable = false, length = 1000)
    private String styleName;

    @Override
    public String toString() {
        return "GameStyle{id=" + id + ", name='" + styleName + "'}";
    }

}
