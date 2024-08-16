package com.gamegoo.scripts;

import com.gamegoo.domain.matching.MatchingRecord;
import com.gamegoo.domain.matching.MatchingStatus;
import com.gamegoo.domain.matching.MatchingType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.repository.matching.MatchingRecordRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class MatchingRecordInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MatchingRecordRepository matchingRecordRepository;
    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 매칭 테스트 코드
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isUpdateMode(event)) {
            // MatchingRecord 테이블을 TRUNCATE
            jdbcTemplate.execute("TRUNCATE TABLE matching_record");

            // Member ID가 29~35인 멤버들을 가져오기
            List<Member> members = memberRepository.findAllByIdBetween(29L, 35L);

            if (!members.isEmpty()) {
                // 20개의 랜덤 매칭 레코드 생성
                Random random = new Random();
                List<MatchingRecord> records = IntStream.range(0, 20)
                        .mapToObj(i -> {
                            Member randomMember = members.get(random.nextInt(members.size()));

                            return MatchingRecord.builder()
                                    .member(randomMember)
                                    .gameMode(1) // Example game mode
                                    .mainPosition(random.nextInt(6))
                                    .subPosition(random.nextInt(6))
                                    .wantPosition(random.nextInt(6))
                                    .mike(random.nextBoolean())
                                    .tier(randomMember.getTier()) // Member의 tier 사용
                                    .rank(randomMember.getRank()) // Member의 rank 사용
                                    .winRate(randomMember.getWinRate()) // Member의 winRate 사용
                                    .status(MatchingStatus.FAIL)
                                    .matchingType(i % 2 == 0 ? MatchingType.BASIC : MatchingType.PRECISE)
                                    .mannerLevel(randomMember.getMannerLevel()) // Member의 mannerLevel 사용
                                    .build();
                        })
                        .collect(Collectors.toList());

                matchingRecordRepository.saveAll(records);
            } else {
                System.out.println("No members found in the specified ID range.");
            }
        }
    }

    private boolean isUpdateMode(ApplicationReadyEvent event) {
        String ddlAuto = event.getApplicationContext().getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto");
        return "create".equalsIgnoreCase(ddlAuto);
    }
}
