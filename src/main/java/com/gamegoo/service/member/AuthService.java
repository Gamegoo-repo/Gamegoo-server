package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.EmailVerifyRecord;
import com.gamegoo.domain.champion.Champion;
import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.member.LoginType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.member.ChampionRepository;
import com.gamegoo.repository.member.EmailVerifyRecordRepository;
import com.gamegoo.repository.member.MemberChampionRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.CodeGeneratorUtil;
import com.gamegoo.util.JWTUtil;
import com.gamegoo.util.RiotUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public Member joinMember(String email, String password, String gameName, String tag,
        Boolean isAgree) {

        // 중복 확인하기
        if (memberRepository.existsByEmail(email)) {
            Member member = memberRepository.findByEmail(email).get();
            if (member.getBlind()) {
                throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
            }
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
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);
        Member member = Member.builder()
            .email(email)
            .password(bCryptPasswordEncoder.encode(password))
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(false)
            .mike(false)
            .mannerLevel(1)
            .isAgree(isAgree)
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
    public void sendEmail(String email, Boolean isCheck) {
        // 중복 확인하기
        boolean isPresent = memberRepository.findByEmail(email).isPresent();
        if (isPresent && isCheck) {
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
            .orElseThrow(() -> new MemberHandler(ErrorStatus.REFRESHTOKEN_NULL));

        // refresh 토큰에서 id 가져오기
        Long id = member.getId();

        // 토큰 생성하기
        String access_token = jwtUtil.createJwtWithId(id, 60 * 60 * 1000L);         // 1시간
        String new_refresh_token = jwtUtil.createJwt(60 * 60 * 24 * 30 * 1000L);    // 30일

        // refresh token 저장하기
        member.updateRefreshToken(new_refresh_token);
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
        EmailVerifyRecord emailVerifyRecord = emailVerifyRecordRepository.findByEmailOrderByUpdatedAtDesc(
                email, PageRequest.of(0, 1))
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
            log.info("Starting email send process for email: {}, certificationNumber: {}", email,
                certificationNumber);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getCertificationMessage(certificationNumber);

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("GamgGoo 이메일 인증 코드");
            mimeMessageHelper.setText(htmlContent, true);
            log.debug("Prepared email message for email: {}", email);

            javaMailSender.send(message);
            log.info("Email sent successfully to email: {}", email);


        } catch (MessagingException e) {
            log.error("Failed to send email to email: {}, certificationNumber: {}, error: {}",
                email, certificationNumber, e.getMessage());

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
        String certificationMessage = ""
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
            + "  <head>"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
            + "    <title>Gamegoo 이메일 인증</title>"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
            + "  </head>"
            + "  <body>"
            + "    <table"
            + "      style=\""
            + "        width: 628px;"
            + "        box-sizing: border-box;"
            + "        border-collapse: collapse;"
            + "        background-color: #ffffff;"
            + "        border: 1px solid #c0c0c0;"
            + "        text-align: left;"
            + "        margin: 0 auto;"
            + "      \""
            + "    >"
            + "      <tbody>"
            + "        <tr>"
            + "          <td>"
            + "            <table"
            + "              cellpadding=\"0\""
            + "              cellspacing=\"0\""
            + "              style=\"width: 628px; height: 521px; padding: 53px 42px 42px 62px\""
            + "            >"
            + "              <tbody>"
            + "                <tr>"
            + "                  <td style=\"padding-bottom: 11.61px\">"
            + "                    <img"
            + "                      src=\"https://ifh.cc/g/BY3XG2.png\""
            + "                      style=\"display: block\""
            + "                      width=\"137\""
            + "                      height=\"24\""
            + "                      alt=\"Gamegoo\""
            + "                    />"
            + "                  </td>"
            + "                </tr>"
            + "                <tr>"
            + "                  <td style=\"padding-top: 20px\">"
            + "                    <span"
            + "                      style=\""
            + "                        color: #2d2d2d;"
            + "                        font-family: Pretendard;"
            + "                        font-size: 25px;"
            + "                        font-style: normal;"
            + "                        font-weight: 400;"
            + "                        line-height: 150%;"
            + "                      \""
            + "                    >"
            + "                      인증코드를 확인해주세요"
            + "                    </span>"
            + "                  </td>"
            + "                </tr>"
            + "                <tr>"
            + "                  <td style=\"padding-top: 38px\">"
            + "                    <span"
            + "                      style=\""
            + "                        color: #5a42ee;"
            + "                        color: #2d2d2d;"
            + "                        font-size: 32px;"
            + "                        font-style: normal;"
            + "                        font-weight: 700;"
            + "                        line-height: 150%;"
            + "                        margin-bottom: 30px;"
            + "                      \""
            + "                    >"
            + "                      " + certificationNumber + ""
            + "                    </span>"
            + "                  </td>"
            + "                </tr>"
            + "                <tr>"
            + "                  <td style=\"padding-top: 30px\">"
            + "                    <span"
            + "                      style=\""
            + "                        color: #2d2d2d;"
            + "                        font-family: Pretendard;"
            + "                        font-size: 18px;"
            + "                        font-style: normal;"
            + "                        font-weight: 400;"
            + "                        line-height: 150%;"
            + "                      \""
            + "                    >"
            + "                      이메일 인증 절차에 따라 이메일 인증코드를"
            + "                      발급해드립니다.<br />"
            + "                      인증코드는 이메일 발송시점으로부터 3분 동안 유효합니다.<br /><br />"
            + "                      만약 본인 요청에 의한 이메일 인증이 아니라면,"
            + "                      고객센터(0000-0000) 또는 cs@gamegoo.com으로 관련 내용을"
            + "                      전달해 주세요.<br /><br />"
            + "                      감사합니다."
            + "                    </span>"
            + "                  </td>"
            + "                </tr>"
            + "              </tbody>"
            + "            </table>"
            + "            <table"
            + "              cellpadding=\"0\""
            + "              cellspacing=\"0\""
            + "              style=\""
            + "                width: 628px;"
            + "                height: 292px;"
            + "                padding: 37px 0px 51px 62px;"
            + "                background: #f7f7f9;"
            + "              \""
            + "            >"
            + "              <tbody>"
            + "                <tr>"
            + "                  <td>"
            + "                    <span"
            + "                      style=\""
            + "                        color: #606060;"
            + "                        font-family: Pretendard;"
            + "                        font-size: 11px;"
            + "                        font-style: normal;"
            + "                        font-weight: 500;"
            + "                        line-height: 150%;"
            + "                      \""
            + "                    >"
            + "                      본 메일은 발신 전용으로 회신되지 않습니다.<br />"
            + "                      궁금하신 점은 겜구 고객센터를 통해 문의하시기 바랍니다.<br /><br />"
            + "                      Email: gamegoo0707@gmail.com<br /><br />"
            + "                      06236 서울특별시 강남구 테헤란로20길5, 8층 겜구<br />"
            + "                      대표이사 : 김예림 | 개인정보보호책임자 : 김예림<br />"
            + "                      사업자 등록번호 [842-86-00373]<br />"
            + "                      통신판매업 신고 : 제 2017-서울강남-00718호<br /><br /><br />"
            + "                      © 겜구(주) All rights reserved. gamegoo.co.kr"
            + "                    </span>"
            + "                  </td>"
            + "                </tr>"
            + "              </tbody>"
            + "            </table>"
            + "          </td>"
            + "        </tr>"
            + "      </tbody>"
            + "    </table>"
            + "  </body>"
            + "</html>";

        return certificationMessage;
    }


    /**
     * refresh 토큰 DB에서 삭제 (로그아웃)
     *
     * @param id
     */
    @Transactional
    public void deleteRefreshToken(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateRefreshToken(null);
        memberRepository.save(member);
    }


}
