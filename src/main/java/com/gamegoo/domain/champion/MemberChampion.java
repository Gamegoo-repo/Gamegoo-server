package com.gamegoo.domain.champion;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@Builder
@Table(name = "MemberChampion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberChampion extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_champion_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 연관관계 메소드
    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberChampionList().remove(this);
        }
        this.member = member;
        this.member.getMemberChampionList().add(this);

    }

    public void removeMember(Member member) {
        member.getMemberChampionList().remove(this);
        this.member = null;
    }
}
