package com.gamegoo.dto.member;

import lombok.*;

import java.util.List;

public class MemberResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class blockListDTO {
        List<blockedMemberDTO> blockedMemberDTOList;
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
    public static class blockedMemberDTO {
        Long memberId;
        Integer profileImg;
        String email;
        String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LoginResponseDTO {

        String accessToken;
        String refreshToken;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChampionResponseDTO {

        Long championId;
        String championName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RefreshTokenResponseDTO {

        String accessToken;
        String refreshToken;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class myProfileMemberDTO {

        Long id;
        Integer profileImg;
        Boolean mike;
        String email;
        String gameName;
        String tag;
        String tier;
        String rank;
        String updatedAt;
        List<GameStyleResponseDTO> gameStyleResponseDTOList;
        List<ChampionResponseDTO> championResponseDTOList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class friendInfoDTO {

        Long memberId;
        String name;
        Integer memberProfileImg;
        boolean isLiked;
    }
}
