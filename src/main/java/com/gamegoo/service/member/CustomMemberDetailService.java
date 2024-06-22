package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberNotFoundExceptionHandler;
import com.gamegoo.apiPayload.exception.handler.UserDeactivatedExceptionHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.CustomMemberDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 로그인 DB 로직
@Service
public class CustomMemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public CustomMemberDetailService(MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // DB에서 조회
        Optional<Member> member = memberRepository.findByEmail(email);
        Member memberData;

        if (member.isPresent()) {
            memberData = member.get();

            if (memberData.getBlind()) {
                throw new UserDeactivatedExceptionHandler(ErrorStatus.USER_DEACTIVATED);
            }
        } else {
            throw new MemberNotFoundExceptionHandler(ErrorStatus.MEMBER_NOT_FOUND);

        }


        // memberDetails에 담아서 return하면 AuthenticationManager가 검증함
        return new CustomMemberDetails(memberData);
    }
}
