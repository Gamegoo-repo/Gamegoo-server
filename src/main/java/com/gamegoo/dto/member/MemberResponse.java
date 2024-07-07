package com.gamegoo.dto.member;

import lombok.*;

import java.util.List;

public class MemberResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class blockListDto {
        List<blockedMemberDto> blockedMemberDtoList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class blockedMemberDto {
        Long memberId;
        String profileImg;
        String email;
        String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LoginResponseDTO {
        String access_token;
        String refresh_token;
        String name;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameStyleResponseDTO {
        Long gameStyleId;
        String gameStyleName;
    }


}
