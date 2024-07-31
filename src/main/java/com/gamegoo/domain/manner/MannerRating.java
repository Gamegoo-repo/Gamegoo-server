package com.gamegoo.domain.manner;

import com.gamegoo.domain.Member;
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
    @Column(name = "manner_rating_id")
    private Long id;

    @OneToMany(mappedBy = "mannerRating", cascade = CascadeType.ALL)
    private List<MannerRatingKeyword> mannerRatingKeywordList = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    // 연관관계 메소드
    public void setFromMember(Member member){
        if (this.fromMember != null){
            this.fromMember.getMannerRatingList().remove(this);
        }
        this.fromMember = member;
        this.fromMember.getMannerRatingList().add(this);
    }
}
