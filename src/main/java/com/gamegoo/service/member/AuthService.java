package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.EmailVerifyRecord;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.champion.Champion;
import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.enums.LoginType;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.member.ChampionRepository;
import com.gamegoo.repository.member.EmailVerifyRecordRepository;
import com.gamegoo.repository.member.MemberChampionRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.CodeGeneratorUtil;
import com.gamegoo.util.JWTUtil;
import com.gamegoo.util.RiotUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final ChampionRepository championRepository;
    private final MemberChampionRepository memberChampionRepository;
    private final EmailVerifyRecordRepository emailVerifyRecordRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;
    private final JWTUtil jwtUtil;
    private final RiotUtil riotUtil;

    /**
     * 회원가입 : Riot 연동 포함
     *
     * @param email
     * @param password
     * @param gameName
     * @param tag
     * @return
     */
    @Transactional
    public Member joinMember(String email, String password, String gameName, String tag) {

        // 중복 확인하기
        if (memberRepository.existsByEmail(email)) {
            throw new MemberHandler(ErrorStatus.MEMBER_CONFLICT);
        }

        // puuid 조회
        String puuid = riotUtil.getRiotPuuid(gameName, tag);

        // 최근 선호 챔피언 3개 리스트 조회
        List<Integer> top3Champions = null;
        try {
            top3Champions = riotUtil.getPreferChampionfromMatch(gameName, puuid);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus.RIOT_MATCH_NOT_FOUND);
        }

        // tier, rank, winrate
        // DB 저장
        // 1. Riot 정보 제외 저장
        Member member = Member.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .loginType(LoginType.GENERAL)
                .profileImage("default")
                .blind(false)
                .build();


        // 2. tier, rank, winrate 저장
        String encryptedSummonerId = riotUtil.getSummonerId(puuid);
        riotUtil.addTierRankWinRate(member, gameName, encryptedSummonerId, tag);


        // 3. 캐릭터와 유저 데이터 매핑해서 DB에 저장하기
        //    (1) 해당 email을 가진 사용자의 정보가 MemberChampion 테이블에 있을 경우 제거
        if (member.getMemberChampionList() != null) {
            member.getMemberChampionList()
                    .forEach(memberChampion -> {
                        memberChampion.removeMember(member); // 양방향 연관관계 제거
                        memberChampionRepository.delete(memberChampion);
                    });

        }

        memberRepository.save(member);

        //    (2) Champion id, Member id 엮어서 MemberChampion 테이블에 넣기
        top3Champions
                .forEach(championId -> {
                    System.out.println(championId);
                    Champion champion = championRepository.findById(Long.valueOf(championId))
                            .orElseThrow(() -> new MemberHandler(ErrorStatus.CHAMPION_NOT_FOUND));

                    MemberChampion memberChampion = MemberChampion.builder()
                            .champion(champion)
                            .build();

                    memberChampion.setMember(member);
                    memberChampionRepository.save(memberChampion);
                });


        return member;
    }

    /**
     * 이메일 인증코드 발송 & 이메일 전송 기록 저장
     *
     * @param email
     */
    @Transactional
    public void sendEmail(String email) {
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
    }

    /**
     * jwt refresh 토큰 검증
     *
     * @param refresh_token
     * @return
     */
    @Transactional
    public MemberResponse.RefreshTokenResponseDTO verifyRefreshToken(String refresh_token) {
        // refresh Token 검증하기
        Member member = memberRepository.findByRefreshToken(refresh_token)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.INVALID_TOKEN));

        // refresh 토큰에서 id 가져오기
        Long id = member.getId();

        // 토큰 생성하기
        String access_token = jwtUtil.createJwtWithId(id, 60 * 60 * 1000L);         // 1시간
        String new_refresh_token = jwtUtil.createJwt(60 * 60 * 24 * 30 * 1000L);    // 30일

        // refresh token 저장하기
        member.setRefreshToken(new_refresh_token);
        memberRepository.save(member);

        return new MemberResponse.RefreshTokenResponseDTO(access_token, new_refresh_token);
    }

    /**
     * 이메일 인증코드 검증
     *
     * @param email
     * @param code
     */
    public void verifyEmail(String email, String code) {
        // 이메일로 보낸 인증 코드 중 가장 최근의 데이터를 불러옴
        EmailVerifyRecord emailVerifyRecord = emailVerifyRecordRepository.findByEmailOrderByUpdatedAtDesc(email, PageRequest.of(0, 1))
                // 가장 최신 기록만 가져오기
                .stream().findFirst()
                // 해당 이메일이 없을 경우
                .orElseThrow(() -> new MemberHandler(ErrorStatus.EMAIL_NOT_FOUND));


        LocalDateTime createdAt = emailVerifyRecord.getCreatedAt();
        LocalDateTime currentAt = LocalDateTime.now();

        // 두 LocalDateTime 객체의 차이를 계산합니다.
        Duration duration = Duration.between(createdAt, currentAt);

        // 차이가 3분 이상인지 확인합니다.
        if (duration.toMinutes() >= 3) {
            throw new MemberHandler(ErrorStatus.EMAIL_INVALID_TIME);
        }

        // 인증 코드가 틀릴 경우
        if (!emailVerifyRecord.getCode().equals(code)) {
            throw new MemberHandler(ErrorStatus.EMAIL_INVALID_CODE);
        }
    }

    /**
     * Gmail 발송
     *
     * @param email
     * @param certificationNumber
     */
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

    /**
     * 메일 내용 편집
     *
     * @param certificationNumber
     * @return
     */
    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h1 style='text-align: center;'> [GAMEGOO 인증메일]</h1>";
        certificationMessage += "<h3 style='text-align: center;'> 인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";

        return certificationMessage;
    }

    /**
     * refresh 토큰 DB에서 삭제 (로그아웃)
     *
     * @param id
     */
    @Transactional
    public void logoutMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.setRefreshToken(null);
        memberRepository.save(member);
    }


}
