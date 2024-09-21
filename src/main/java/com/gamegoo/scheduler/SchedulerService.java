package com.gamegoo.scheduler;

import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.matching.MatchingRecord;
import com.gamegoo.domain.matching.MatchingStatus;
import com.gamegoo.repository.matching.MatchingRecordRepository;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final MatchingRecordRepository matchingRecordRepository;
    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private static final Long MANNER_MESSAGE_TIME = 60L; // 매칭 성공 n초 이후의 엔티티 조회함
    private static final String MANNER_SYSTEM_MESSAGE = "매칭은 어떠셨나요? 상대방의 매너를 평가해주세요!";


    /**
     * 매칭 성공 1시간이 경과한 경우, 두 사용자에게 매너평가 시스템 메시지 전송
     */
    @Transactional
    @Scheduled(fixedRate = 1000 * 60) // 60초 주기로 실행
    public void mannerSystemMessageRun() {
        log.info("scheduler start");

        // 매칭 성공 1분이 경과된 matchingRecord 엔티티 조회 (실제로는 60분으로 해야함)
        LocalDateTime updatedTime = LocalDateTime.now().plusSeconds(MANNER_MESSAGE_TIME);
        List<MatchingRecord> matchingRecordList = matchingRecordRepository.findByStatusAndMannerMessageSentAndUpdatedAtBefore(
            MatchingStatus.SUCCESS, false, updatedTime);

        matchingRecordList.forEach(matchingRecord -> {
            chatQueryService.getChatroomByMembers(
                matchingRecord.getMember(),
                matchingRecord.getTargetMember()
            ).ifPresentOrElse(
                chatroom -> {
                    Chat andSaveSystemChat = chatCommandService.createAndSaveSystemChat(
                        chatroom, matchingRecord.getMember(), MANNER_SYSTEM_MESSAGE, null);
                    matchingRecord.updateMannerMessageSent(true);
                },
                () -> log.info("Chatroom not found, member ID: {}, target member ID: {}",
                    matchingRecord.getMember().getId(), matchingRecord.getTargetMember().getId()));
        });

    }
}
