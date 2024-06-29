package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.EmailVerifyRecord;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.enums.LoginType;
import com.gamegoo.dto.member.JoinDTO;
import com.gamegoo.repository.member.EmailVerifyRecordRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final EmailVerifyRecordRepository emailVerifyRecordRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;

    // 회원가입 로직
    public void joinMember(JoinDTO joinDTO) {

        // DTO로부터 데이터 받기
        String email = joinDTO.getEmail();
        String password = joinDTO.getPassword();

        // 중복 확인하기
        boolean isPresent = memberRepository.findByEmail(email).isPresent();
        if (isPresent) {
            throw new MemberHandler(ErrorStatus.MEMBER_CONFLICT);
        }

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

    //이메일 인증코드 전송
    public String sendEmail(String email) {
        // 중복 확인하기
        boolean isPresent = memberRepository.findByEmail(email).isPresent();
        if (isPresent) {
            throw new MemberHandler(ErrorStatus.MEMBER_CONFLICT);
        }

        // 랜덤 코드 생성하기
        String certificationNumber = CodeGeneratorUtil.generateRandomCode();

        // 메일 전송하기
        sendEmailInternal(email, certificationNumber);

        // 메일 전송 기록 DB에 저장하기
        EmailVerifyRecord emailVerifyRecord = EmailVerifyRecord.builder()
                .email(email)
                .code(certificationNumber)
                .build();

        emailVerifyRecordRepository.save(emailVerifyRecord);

        // 인증 번호 반환
        return certificationNumber;
    }

    // 이메일 인증코드 검증
    public void verifyEmail(String email, String code) {
        // 이메일로 보낸 인증 코드 중 가장 최근의 데이터를 불러옴
        EmailVerifyRecord emailVerifyRecord = emailVerifyRecordRepository.findByEmailOrderByUpdatedAtDesc(email)
                // 해당 이메일이 없을 경우
                .orElseThrow(() -> new MemberHandler(ErrorStatus.EMAIL_NOT_FOUND));

        // 인증 코드가 틀릴 경우
        if (!emailVerifyRecord.getCode().equals(code)) {
            throw new MemberHandler(ErrorStatus.EMAIL_INVALID);
        }
    }

    // 메일 전송하는 메소드
    private void sendEmailInternal(String email, String certificationNumber) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getCertificationMessage(certificationNumber);

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("GamgGoo 이메일 인증 코드");
            mimeMessageHelper.setText(htmlContent, true);
            System.out.println(certificationNumber + " " + email);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new MemberHandler(ErrorStatus.EMAIL_SEND_ERROR);
        }
    }

    // 메일 내용
    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h1 style='text-align: center;'> [GAMEGOO 인증메일]</h1>";
        certificationMessage += "<h3 style='text-align: center;'> 인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";

        return certificationMessage;
    }

}
