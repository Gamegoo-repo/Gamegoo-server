package com.gamegoo.converter;

import com.gamegoo.dto.matching.MatchingRequest;
import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.matching.MemberPriority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchingConverter {

    // 매칭 요청에 대한 응답 DTO 생성
    public static MatchingResponse.PriorityMatchingResponseDTO toPriorityMatchingResponseDTO(
            Member member,
            MatchingRequest.InitializingMatchingRequestDTO request,
            List<MemberPriority> myPriorityList,
            List<MemberPriority> otherPriorityList,
            List<String> gameStyleList) {

        // 내 매칭 기록 DTO 생성
        MatchingResponse.matchingRequestResponseDTO myMatchingInfo = MatchingResponse.matchingRequestResponseDTO.builder()
                .memberId(member.getId())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(member.getRank())
                .mannerLevel(member.getMannerLevel())
                .profileImg(member.getProfileImage())
                .gameMode(request.getGameMode())
                .mainPosition(request.getMainP())
                .subPosition(request.getSubP())
                .wantPosition(request.getWantP())
                .mike(request.getMike())
                .gameStyleList(gameStyleList)
                .build();

        // 최종 DTO 반환
        return MatchingResponse.PriorityMatchingResponseDTO.builder()
                .myPriorityList(myPriorityList)
                .otherPriorityList(otherPriorityList)
                .myMatchingInfo(myMatchingInfo)
                .build();
    }
}
