package com.gamegoo.domain.gamestyle;

import com.gamegoo.domain.Member.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "MemberGameStyle")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberGameStyle extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_gamestyle_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gamestyle_id", nullable = false)
    private GameStyle gameStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 연관관게 메소드
    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberGameStyleList().remove(this);
        }
        this.member = member;
        this.member.getMemberGameStyleList().add(this);
    }

    // MemberGameStyle 삭제를 위한 메소드
    public void removeMember(Member member) {
        member.getMemberGameStyleList().remove(this);
        this.member = null;
    }
}
