package com.gamegoo.converter;

import com.gamegoo.dto.matching.MatchingResponse;
import com.gamegoo.dto.matching.MemberPriority;

import java.util.List;

public class MatchingConverter {
    public static MatchingResponse.PriorityMatchingResponseDTO toPriorityMatchingResponseDTO(List<MemberPriority> myPriority, List<MemberPriority> otherPriority) {
        return MatchingResponse.PriorityMatchingResponseDTO.builder()
                .myPriorityList(myPriority)
                .otherPriorityList(otherPriority)
                .build();
    }

}
