package com.gamegoo.domain.gamestyle;

import com.gamegoo.domain.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MemberGameStyle")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberGameStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_gamestyle_id", nullable = false)
    private Long id;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gamestyle_id", nullable = false)
    private GameStyle gameStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
