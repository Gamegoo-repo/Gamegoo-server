package com.gamegoo.domain;

import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.member.Member;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Boolean isDeleted;

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
