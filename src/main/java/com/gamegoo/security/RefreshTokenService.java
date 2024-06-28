package com.gamegoo.security;

import com.gamegoo.domain.Member;
import com.gamegoo.jwt.JWTUtil;
import com.gamegoo.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public void saveRefreshToken(Long id, String refresh_token) {
        Member member = memberRepository.findById(id)
                .orElseThrow();
        member.setRefreshToken(refresh_token);
        memberRepository.save(member);

    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        return jwtUtil.isExpired(refreshToken);
    }

}
