package com.gamegoo.dto.member;

import lombok.*;

import java.util.List;

public class MemberResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class blockListDTO {
        List<blockedMemberDTO> blocked_member_dto_list;
        Integer list_size;
        Integer total_page;
        Long total_elements;
        Boolean is_first;
        Boolean is_last;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class blockedMemberDTO {
        Long member_id;
        String profile_img;
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

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RefreshTokenResponseDTO {
        String access_token;
        String refresh_token;
    }
}
