package com.gamegoo.dto.matching;


import com.gamegoo.domain.member.Tier;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MatchingResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityMatchingResponseDTO {

        List<MemberPriority> myPriorityList;
        List<MemberPriority> otherPriorityList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class matchingFoundResponseDTO {

        matchingRequestResponseDTO myMatchingInfo;
        matchingRequestResponseDTO targetMatchingInfo;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class matchingRequestResponseDTO {

        Long memberId;
        String gameName;
        String tag;
        Tier tier;
        Integer rank;
        Integer mannerLevel;
        Integer profileImg;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean mike;
        List<String> gameStyleList;
    }

}
