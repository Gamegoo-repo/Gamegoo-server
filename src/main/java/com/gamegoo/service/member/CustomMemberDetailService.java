package com.gamegoo.service.member;

import com.gamegoo.domain.Member;
import com.gamegoo.repository.MemberRepository;
import com.gamegoo.security.CustomMemberDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 로그인 DB 로직
@Service
public class CustomMemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public CustomMemberDetailService(MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        //DB에서 조회
        Member memberData = memberRepository.findByEmail(email);

        if (memberData != null) {

            //memberDetails에 담아서 return하면 AutneticationManager가 검증 함
            return new CustomMemberDetails(memberData);
        }

        return null;
    }
}
