package com.gamegoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
