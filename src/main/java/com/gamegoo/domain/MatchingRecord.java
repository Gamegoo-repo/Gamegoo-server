package com.gamegoo.domain;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "MatchingRecord")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingRecord extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id", nullable = false)
    private Long id;

    @Column(name = "game_mode", nullable = false)
    private Integer gameMode;

    @Column(name = "main_position", nullable = false)
    private Integer mainPosition;

    @Column(name = "sub_position", nullable = false)
    private Integer subPosition;

    @Column(name = "want_position", nullable = false)
    private Integer wantPosition;

    @Column(name = "mike", nullable = false)
    private Boolean mike;

    @Column(name = "tier", nullable = false)
    private Integer tier;

    @Column(name = "is_complete", nullable = false)
    private Boolean isComplete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
