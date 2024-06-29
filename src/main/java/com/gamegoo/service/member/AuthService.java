package com.gamegoo.service.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.enums.LoginType;
import com.gamegoo.dto.member.JoinDTO;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;
    private final HttpServletResponse response;

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
