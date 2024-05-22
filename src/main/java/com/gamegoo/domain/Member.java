package com.gamegoo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "Member")
@Getter
@Setter
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

}

