package com.gamegoo.domain;

import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "Block")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Block extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Member blockerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Member blockedMember;

    // 연관관계 편의 메소드
    public void setBlockerMember(Member member) {
        if (this.blockerMember != null) {
            this.blockerMember.getBlockList().remove(this);
        }
        this.blockerMember = member;
        member.getBlockList().add(this);

    }

    // Block 엔티티 삭제를 위한 메소드
    public void removeBlockerMember(Member blockerMember) {
        blockerMember.getBlockList().remove(this);
        this.blockerMember = null;
    }
}
