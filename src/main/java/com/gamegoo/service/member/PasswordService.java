package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.member.Member;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 비밀번호가 맞는지 확인
     *
     * @param userId
     * @param password
     * @return
     */
    public boolean checkPasswordById(Long userId, String password) {
        return memberRepository.findById(userId)
                .map(member -> bCryptPasswordEncoder.matches(password, member.getPassword()))
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    /**
     * 비밀번호 수정
     *
     * @param userId
     * @param newPassword
     */
    public void updatePassword(Long userId, String newPassword) {
        // jwt 토큰으로 멤버 찾기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비밀번호 재설정
        member.updatePassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    /**
     * 비밀번호 수정
     *
     * @param email
     * @param newPassword
     */
    public void updatePasswordWithEmail(String email, String newPassword) {
        // jwt 토큰으로 멤버 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 비밀번호 재설정
        member.updatePassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

}
