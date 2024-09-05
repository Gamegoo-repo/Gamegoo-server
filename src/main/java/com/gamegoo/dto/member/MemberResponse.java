package com.gamegoo.dto.member;

import com.gamegoo.domain.member.Tier;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        Integer profileImage;

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
        Tier tier;
        Integer rank;
        Integer manner;
        String updatedAt;
        Integer mainP;
        Integer subP;
        Boolean isAgree;
        Boolean isBlind;
        String loginType;
        Double winrate;
        List<GameStyleResponseDTO> gameStyleResponseDTOList;
        List<ChampionResponseDTO> championResponseDTOList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class memberProfileDTO {

        Long id;
        Integer profileImg;
        Boolean mike;
        String email;
        String gameName;
        String tag;
        Tier tier;
        Integer rank;
        Integer manner;
        String updatedAt;
        Integer mainP;
        Integer subP;
        Boolean isAgree;
        Boolean isBlind;
        String loginType;
        Double winrate;
        Boolean blocked; // 해당 회원을 차단했는지 여부
        Boolean friend; // 해당 회원과의 친구 여부
        Long friendRequestMemberId; // 해당 회원과의 친구 요청 상태
        List<GameStyleResponseDTO> gameStyleResponseDTOList;
        List<ChampionResponseDTO> championResponseDTOList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class friendListDTO {

        List<friendInfoDTO> friendInfoDTOList;
        Integer list_size;
        Boolean has_next;
        Long next_cursor;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class friendInfoDTO {

        Long memberId;
        String name;
        Integer memberProfileImg;
        Boolean isLiked;
        Boolean isBlind;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class friendRequestResultDTO {

        Long targetMemberId;
        String result;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class starFriendResultDTO {

        Long friendMemberId;
        String result;
    }

}
