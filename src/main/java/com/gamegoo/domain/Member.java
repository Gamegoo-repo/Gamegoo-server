package com.gamegoo.domain;

import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.common.BaseDateTimeEntity;
import com.gamegoo.domain.enums.LoginType;
import com.gamegoo.domain.gamestyle.MemberGameStyle;
import com.gamegoo.domain.manner.MannerRating;
import com.gamegoo.domain.notification.Notification;
import com.gamegoo.domain.report.Report;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Member")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 500)
    private String password;

    @Column(name = "profile_image", length = 30)
    private String profileImage = "default";

    @Column(name = "manner_level")
    private Integer mannerLevel = 0;

    @Column(name = "blind", nullable = false)
    private Boolean blind = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private LoginType loginType;

    @Column(name = "gameuser_name", length = 100)
    private String gameuserName;

    @Column(name = "tier")
    private String tier;

    @Column(name = "rank")
    private String rank;

    @Column(name = "winrate")
    private double winRate;

    @Column(name = "main_position")
    private int mainPosition;

    @Column(name = "sub_position")
    private int subPosition;

    @Column(name = "refresh_token")
    private String refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberChampion> memberChampionList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberGameStyle> memberGameStyleList = new ArrayList<>();

    @OneToMany(mappedBy = "toMember", cascade = CascadeType.ALL)
    private List<MannerRating> mannerRatingList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList = new ArrayList<>();

    @OneToMany(mappedBy = "blockerMember", cascade = CascadeType.ALL)
    private List<Block> blockList = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private List<Report> reportList = new ArrayList<>();

}

