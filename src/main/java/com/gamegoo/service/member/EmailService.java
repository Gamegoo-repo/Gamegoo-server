package com.gamegoo.service.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.security.CodeGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Service
public class EmailService {
    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private final HttpServletResponse response;

    @Autowired
    public EmailService(MemberRepository memberRepository, JavaMailSender javaMailSender, HttpServletResponse response) {
        this.memberRepository = memberRepository;
        this.javaMailSender = javaMailSender;
        this.response = response;
    }

    //이메일 인증 및 전송
    public String verifyEmail(String email) throws IOException {
        // 중복 확인하기
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            ErrorStatus errorStatus = ErrorStatus.MEMBER_CONFLICT;
            ApiResponse<Object> apiResponse = ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage(), null);

            response.setStatus(errorStatus.getHttpStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            new ObjectMapper().writeValue(response.getWriter(), apiResponse);
        }

        // 랜덤 코드 생성하기
        String certificationNumber = CodeGeneratorUtil.generateRandomCode();

        // 메일 보내기
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
            throw new RuntimeException(e);
        }
        return certificationNumber;
    }

    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h1 style='text-align: center;'> [GAMEGOO 인증메일]</h1>";
        certificationMessage += "<h3 style='text-align: center;'> 인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";

        return certificationMessage;
    }
}
