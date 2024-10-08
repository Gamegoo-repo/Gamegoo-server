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

        // 회원가입 완료된 사용자 정보 로그로 출력
        log.info("회원가입 완료 - 이메일: {}, 프로필 이미지: {}, 소환사명: {}, 태그: {}, 티어: {}, 랭크: {}",
                member.getEmail(), member.getProfileImage(), member.getGameName(), member.getTag(), member.getTier(), member.getRank());


        return member;
    }

    /**
     * 이메일 중복 확인 검증
     * @param email
     */
    public void verifyEmail(String email){
        // 해당 이메일이 DB에 있는지 확인하기
        boolean isPresent = memberRepository.findByEmail(email).isPresent();

        if (isPresent) {
            // 중복확인 (회원가입 전용)
            throw new MemberHandler(ErrorStatus.MEMBER_CONFLICT);
        }else{
            // DB에 없는 사용자인지 확인 (비밀번호 찾기 전용)
            throw new MemberHandler(ErrorStatus.EMAIL_INVALID_USER);
        }

    }

    /**
     * 이메일 인증코드 발송 & 이메일 전송 기록 저장
     *
     * @param email
     */
    @Transactional
    public void sendEmail(String email) {

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

        return new MemberResponse.RefreshTokenResponseDTO(id,access_token, new_refresh_token);
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
                +"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "  <head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "    <title>Gamegoo 이메일 인증</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <table\n" +
                "      style=\"\n" +
                "        width: 628px;\n" +
                "        box-sizing: border-box;\n" +
                "        border-collapse: collapse;\n" +
                "        background-color: #ffffff;\n" +
                "        border: 1px solid #c0c0c0;\n" +
                "        text-align: left;\n" +
                "        margin: 0 auto;\n" +
                "      \"\n" +
                "    >\n" +
                "      <tbody>\n" +
                "        <tr>\n" +
                "          <td>\n" +
                "            <table\n" +
                "              cellpadding=\"0\"\n" +
                "              cellspacing=\"0\"\n" +
                "              style=\"width: 628px; height: 521px; padding: 53px 62px 42px 62px\"\n" +
                "            >\n" +
                "              <tbody>\n" +
                "                <tr>\n" +
                "                  <td style=\"padding-bottom: 11.61px\">\n" +
                "                    <img\n" +
                "                      src=\"https://ifh.cc/g/BY3XG2.png\"\n" +
                "                      style=\"display: block\"\n" +
                "                      width=\"137\"\n" +
                "                      height=\"24\"\n" +
                "                      alt=\"Gamegoo\"\n" +
                "                    />\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                  <td style=\"padding-top: 20px\">\n" +
                "                    <span\n" +
                "                      style=\"\n" +
                "                        color: #2d2d2d;\n" +
                "                        font-family: Pretendard;\n" +
                "                        font-size: 25px;\n" +
                "                        font-style: normal;\n" +
                "                        font-weight: 400;\n" +
                "                        line-height: 150%;\n" +
                "                      \"\n" +
                "                    >\n" +
                "                      인증코드를 확인해주세요\n" +
                "                    </span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                  <td style=\"padding-top: 38px\">\n" +
                "                    <span\n" +
                "                      style=\"\n" +
                "                        color: #5a42ee;\n" +
                "                        color: #2d2d2d;\n" +
                "                        font-size: 32px;\n" +
                "                        font-style: normal;\n" +
                "                        font-weight: 700;\n" +
                "                        line-height: 150%;\n" +
                "                        margin-bottom: 30px;\n" +
                "                      \"\n" +
                "                    >\n" +
                                      certificationNumber+
                "                    </span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                  <td style=\"padding-top: 30px\">\n" +
                "                    <span\n" +
                "                      style=\"\n" +
                "                        color: #2d2d2d;\n" +
                "                        font-family: Pretendard;\n" +
                "                        font-size: 18px;\n" +
                "                        font-style: normal;\n" +
                "                        font-weight: 400;\n" +
                "                        line-height: 150%;\n" +
                "                      \"\n" +
                "                    >\n" +
                "                      이메일 인증 절차에 따라 이메일 인증코드를 발급해드립니다.\n" +
                "                      인증코드는 이메일 발송시점으로부터 3분 동안 유효합니다.<br /><br />\n" +
                "                      만약 본인 요청에 의한 이메일 인증이 아니라면,<br />\n" +
                "                      gamegoo0707@gmail.com으로 관련 내용을 전달해 주세요.<br /><br />\n" +
                "\n" +
                "                      감사합니다.\n" +
                "                    </span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "            <table\n" +
                "              cellpadding=\"0\"\n" +
                "              cellspacing=\"0\"\n" +
                "              style=\"\n" +
                "                width: 628px;\n" +
                "                height: 292px;\n" +
                "                padding: 37px 0px 153px 62px;\n" +
                "                background: #f7f7f9;\n" +
                "              \"\n" +
                "            >\n" +
                "              <tbody>\n" +
                "                <tr>\n" +
                "                  <td>\n" +
                "                    <span\n" +
                "                      style=\"\n" +
                "                        color: #606060;\n" +
                "                        font-family: Pretendard;\n" +
                "                        font-size: 11px;\n" +
                "                        font-style: normal;\n" +
                "                        font-weight: 500;\n" +
                "                        line-height: 150%;\n" +
                "                      \"\n" +
                "                    >\n" +
                "                      본 메일은 발신 전용으로 회신되지 않습니다.<br />\n" +
                "                      궁금하신 점은 겜구 이메일이나 카카오 채널을 통해\n" +
                "                      문의하시기 바랍니다.<br /><br />\n" +
                "                      email: gamegoo0707@gmail.com<br />\n" +
                "                      kakao: https://pf.kakao.com/_Rrxiqn<br />\n" +
                "                      copyright 2024. GameGoo All Rights Reserved.<br />\n" +
                "                    </span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>\n";

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
