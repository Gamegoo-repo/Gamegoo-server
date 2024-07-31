package com.gamegoo.domain.manner;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MannerKeyword extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manner_keyword_id")
    private Long id;

    @Column(name = "contents", nullable = false, length = 200)
    private String contents;

    @Column(name = "is_positive", nullable = false)
    private Boolean isPositive;
}
