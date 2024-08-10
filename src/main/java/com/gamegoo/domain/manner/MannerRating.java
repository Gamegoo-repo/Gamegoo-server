package com.gamegoo.domain.manner;

import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MannerRating extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manner_rating_id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "mannerRating", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MannerRatingKeyword> mannerRatingKeywordList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @Column(name = "is_Positive")
    private Boolean isPositive;

    // 연관관계 메소드
    public void setToMember(Member toMember) {
        if (this.toMember != null) {
            this.toMember.getMannerRatingList().remove(this);
        }
        this.toMember = toMember;
        if (toMember != null) {
            this.toMember.getMannerRatingList().add(this);
        }
    }

    public void removeMannerRatingKeyword(MannerRatingKeyword mannerRatingKeyword) {
        this.mannerRatingKeywordList.remove(mannerRatingKeyword);
        mannerRatingKeyword.setMannerRating(null);
    }
}
