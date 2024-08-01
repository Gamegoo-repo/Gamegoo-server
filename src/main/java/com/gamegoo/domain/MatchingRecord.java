package com.gamegoo.domain;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "MatchingRecord")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingRecord extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id", nullable = false)
    private Long id;

    // 1: 빠른 대전, 2: 솔로 랭크, 3: 자유 랭크, 4: 칼바람 나락
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

    @Column(name = "tier", columnDefinition = "VARCHAR(20)")
    private String tier;

    @Column(name = "rank", columnDefinition = "VARCHAR(10)")
    private String rank;

    @Column(name = "winrate")
    private Double winRate;

    // FAIL, QUIT, SUCCESS
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(10)")
    private String status;

    // BASIC : 겜구 매칭, PRECISE : 정밀 매칭
    @Column(name = "matching_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private String matchingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
