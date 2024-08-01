package com.gamegoo.service.matching;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MatchingHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.MatchingRecord;
import com.gamegoo.domain.Member;
import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.repository.matching.MatchingRecordRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private final MemberRepository memberRepository;
    private final MatchingRecordRepository matchingRecordRepository;

    @Transactional
    public void save(MatchingRequest.SaveMatchingRequestDTO request, Long id) {
        // 회원 정보 불러오기
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매칭 기록 저장
        MatchingRecord matchingRecord = MatchingRecord.builder()
                .mike(request.getMike())
                .tier(member.getTier())
                .rank(member.getRank())
                .matchingType(request.getMatching_type())
                .status("FAIL")
                .mainPosition(request.getMainP())
                .subPosition(request.getSubP())
                .wantPosition(request.getWantP())
                .winRate(member.getWinRate())
                .gameMode(request.getGameMode())
                .build();

        matchingRecord.setMember(member);
        matchingRecordRepository.save(matchingRecord);

    }

    @Transactional
    public void modify(MatchingRequest.ModifyMatchingRequestDTO request, Long id) {
        // 회원 정보 불러오기
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 매칭 기록 불러오기
        MatchingRecord matchingRecord = matchingRecordRepository.findByMember(member);
        String status = request.getStatus();

        // 매칭 상태 검사 후 변
        if (status.equals("QUIT") || status.equals("SUCCESS")) {
            matchingRecord.setStatus(request.getStatus());
            matchingRecordRepository.save(matchingRecord);
        } else {
            throw new MatchingHandler(ErrorStatus.MATCHING_STATUS_BAD_REQUEST);
        }

    }
}
