package com.gamegoo.security;

import com.gamegoo.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// UserDetails 오버라이딩해서 Spring Security의 Login Filter에 사용하는 클래스
public class CustomUserDetails implements UserDetails {
    private final Member member;

    public CustomUserDetails(Member member) {

        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        // 현 시점에서 여러 사용자의 권한을 구별할 필요가 없기 때문에 "USER"라는 권한으로 통일
// 이후 사용자의 권한을 구별할 필요가 있을 경우 DB에 role column 생성 후 member.getRole() 메소드로 가져오기
        collection.add((GrantedAuthority) () -> "USER");

        return collection;
    }

    @Override
    public String getPassword() {

        return member.getPassword();
    }

    @Override
    public String getUsername() {

        return member.getEmail();
    }

    public Long getId() {
        return member.getId();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}
