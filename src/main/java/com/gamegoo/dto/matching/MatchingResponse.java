package com.gamegoo.dto.matching;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MatchingResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityMatchingResponseDTO {
        List<MemberPriority> myPriorityList;
        List<MemberPriority> otherPriorityList;
    }
}
