package com.gamegoo.dto.matching;


import java.util.List;

public class MatchingResponse {
    public static class PriorityMatchingResponse {
        List<MemberPriority> myPriorityList;
        List<MemberPriority> otherPriorityList;
    }
}
