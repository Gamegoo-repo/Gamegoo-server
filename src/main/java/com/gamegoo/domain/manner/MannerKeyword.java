package com.gamegoo.domain.manner;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;
import org.springframework.lang.Nullable;

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

    @Column(nullable = false, length = 200)
    private String contents;

    @Column(nullable = false)
    private Boolean isPositive;
}
