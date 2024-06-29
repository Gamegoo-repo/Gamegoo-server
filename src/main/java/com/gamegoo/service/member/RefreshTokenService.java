package com.gamegoo.service.member;

import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private MemberRepository memberRepository;
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
