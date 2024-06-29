package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean checkPasswordById(Long userId, String password) {
        return memberRepository.findById(userId)
                .map(member -> bCryptPasswordEncoder.matches(password, member.getPassword()))
                .orElse(false);
    }

    public void updatePassword(Long userId, String newPassword) {
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setPassword(bCryptPasswordEncoder.encode(newPassword));
            memberRepository.save(member);
        } else {
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);
        }
    }
}
