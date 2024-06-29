package com.gamegoo.service.member;

import com.gamegoo.apiPayload.exception.handler.CustomUserException;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 로그인 DB 로직
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> {
                    if (member.getBlind()) {
                        throw new CustomUserException("해당 사용자는 탈퇴한 사용자입니다.");
                    }
                    return new CustomUserDetails(member);
                })
                .orElseThrow(() -> new JwtException("No Member"));
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Member member = memberRepository.findById(id)
                .filter(m -> !m.getBlind())
                .orElseThrow(() -> new JwtException("No Member"));
        return new CustomUserDetails(member);
    }


}

