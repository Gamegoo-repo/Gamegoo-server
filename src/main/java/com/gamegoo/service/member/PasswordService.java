package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.PasswordHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.PasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public PasswordService(PasswordRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.passwordRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public boolean checkPasswordById(Long userId, String password) {
        return passwordRepository.findById(userId)
                .map(member -> bCryptPasswordEncoder.matches(password, member.getPassword()))
                .orElse(false);
    }

    public void updatePassword(Long userId, String newPassword) {
        Optional<Member> optionalMember = passwordRepository.findById(userId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setPassword(bCryptPasswordEncoder.encode(newPassword));
            passwordRepository.save(member);
        } else {
            throw new PasswordHandler(ErrorStatus.PASSWORD_NOT_FOUND);
        }
    }
}
