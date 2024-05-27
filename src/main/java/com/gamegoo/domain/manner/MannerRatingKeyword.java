package com.gamegoo.domain.manner;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MannerRatingKeyword extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "manner_rating_keyword_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manner_rating_id", nullable = false)
    private MannerRating mannerRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manner_keyword_id", nullable = false)
    private MannerKeyword mannerKeyword;

}
