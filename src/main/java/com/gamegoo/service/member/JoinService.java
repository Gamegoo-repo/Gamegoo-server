package com.gamegoo.service.member;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.enums.LoginType;
import com.gamegoo.dto.member.JoinDTO;
import com.gamegoo.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 생성자
    @Autowired
    public JoinService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 회원가입 로직
    public void JoinMember(JoinDTO joinDTO) {

        // DTO로부터 데이터 받기
        String email = joinDTO.getEmail();
        String password = joinDTO.getPassword();
        // 중복 확인은 이메일 인증 코드 발급 API에서 진행 (로직이 이메일 인증 API -> 회원가입 API)

        // DB에 넣을 정보 설정
        Member member = Member.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .loginType(LoginType.GENERAL)
                .profileImage("default")
                .blind(false)
                .build();

        // DB에 저장
        memberRepository.save(member);
    }
}
