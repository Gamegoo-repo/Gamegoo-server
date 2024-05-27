package com.gamegoo.domain;

import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.notification.Notification;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Member")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", nullable = false, length = 30)
    private String email;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "profile_image", length = 10)
    private String profileImage;

    @Column(name = "manner_level")
    private Integer mannerLevel = 0;

    @Column(name = "blind", nullable = false)
    private Boolean blind = false;

    @Column(name = "login_type", nullable = false, length = 30)
    private String loginType = "General";

    @Column(name = "gameuser_name", nullable = true, length = 30)
    private String gameuserName;

    @Column(name = "tier", nullable = false)
    private Integer tier;

    @Column(name = "winrate", nullable = false)
    private Integer winRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberChampion> memberChampionList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<GameStyle> gameStyleList = new ArrayList<>();

    @OneToMany(mappedBy = "toMember", cascade = CascadeType.ALL)
    private List<MannerRating> mannerRatingList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList = new ArrayList<>();

}

