package com.gamegoo.dto.board;

import com.gamegoo.domain.member.Tier;
import com.gamegoo.dto.manner.MannerResponse;
import com.gamegoo.dto.member.MemberResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardInsertResponseDTO {

        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        Integer rank;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean mike;
        List<Long> gameStyles;
        String contents;

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
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardUpdateResponseDTO {

        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        Integer rank;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Boolean mike;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardListResponseDTO {

        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        Integer mannerLevel;
        Tier tier;
        Integer rank;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        List<MemberResponse.ChampionResponseDTO> championResponseDTOList;
        Double winRate;
        LocalDateTime createdAt;
        Boolean mike;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardByIdResponseDTO {

        Long boardId;
        Long memberId;
        LocalDateTime createdAt;
        Integer profileImage;
        String gameName;
        String tag;
        Integer mannerLevel;
        Tier tier;
        Integer rank;
        Boolean mike;
        List<MemberResponse.ChampionResponseDTO> championResponseDTOList;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Integer recentGameCount;
        Double winRate;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class boardByIdResponseForMemberDTO {

        Long boardId;
        Long memberId;
        Boolean isBlocked;
        Boolean isFriend;
        Long friendRequestMemberId;
        LocalDateTime createdAt;
        Integer profileImage;
        String gameName;
        String tag;
        Integer mannerLevel;
        List<MannerResponse.mannerKeywordDTO> mannerKeywords;
        Tier tier;
        Integer rank;
        Boolean mike;
        List<MemberResponse.ChampionResponseDTO> championResponseDTOList;
        Integer gameMode;
        Integer mainPosition;
        Integer subPosition;
        Integer wantPosition;
        Integer recentGameCount;
        Double winRate;
        List<Long> gameStyles;
        String contents;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class myBoardResponseDTO{
        Integer totalPage;
        Integer totalCount;
        List<myBoardListResponseDTO> myBoards;
    }


    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class myBoardListResponseDTO {

        Long boardId;
        Long memberId;
        Integer profileImage;
        String gameName;
        String tag;
        Tier tier;
        Integer rank;
        String contents;
        LocalDateTime createdAt;
    }
}
