package com.gamegoo.domain.member;

import com.gamegoo.domain.Block;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.common.BaseDateTimeEntity;
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

    @Column(name = "profile_image")
    private Integer profileImage;

    @Column(name = "manner_level")
    private Integer mannerLevel = 0;

    @Column(name = "blind", nullable = false)
    private Boolean blind = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private LoginType loginType;

    @Column(name = "gamename", length = 100)
    private String gameName;

    @Column(name = "tag", length = 100)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier")
    private Tier tier;

    @Column(name = "rank")
    private String rank;

    @Column(name = "winrate")
    private Double winRate;

    @Column(name = "main_position")
    private Integer mainPosition;

    @Column(name = "sub_position")
    private Integer subPosition;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "mike")
    private Boolean mike = false;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberChatroom> memberChatroomList = new ArrayList<>();

    public void updatePosition(Integer mainPosition, Integer subPosition) {
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
    }

    public void updateProfileImage(Integer profileImage) {
        this.profileImage = profileImage;
    }

    public void deactiveMember() {
        this.blind = true;
    }

    public void updateMemberFromMatching(Integer mainPosition, Integer subPosition, Boolean mike) {
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        this.mike = mike;
    }

    public void initializeMemberChampionList() {
        this.memberChampionList = new ArrayList<>();
    }

    public void updateRiotDetails(Tier tier, String rank, Double winRate) {
        this.tier = tier;
        this.rank = rank;
        this.winRate = winRate;
    }

    public void updateRiotBasic(String gameName, String tag) {
        this.gameName = gameName;
        this.tag = tag;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

