package com.gamegoo.domain.matchingrecord;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.member.Tier;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", columnDefinition = "VARCHAR(20)")
    private Tier tier;

    @Column(name = "rank", columnDefinition = "VARCHAR(10)")
    private String rank;

    @Column(name = "winrate")
    private Double winRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(10)")
    private MatchingStatus status;

    // BASIC : 겜구 매칭, PRECISE : 정밀 매칭
    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private MatchingType matchingType;

    @Column(name = "manner_level")
    private Integer mannerLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // status 변경
    public void updateStatus(MatchingStatus status) {
        this.status = status;
    }
}
