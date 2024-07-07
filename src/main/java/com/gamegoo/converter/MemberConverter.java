package com.gamegoo.converter;

import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.MemberResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class MemberConverter {

    public static MemberResponse.blockListDto toBlockListDto(Page<Member> blockList) {
        List<MemberResponse.blockedMemberDto> blockedMemberDtoList = blockList.stream()
                .map(MemberConverter::toBlockedMemberDto)
                .collect(Collectors.toList());

        return MemberResponse.blockListDto.builder()
                .blockedMemberDtoList(blockedMemberDtoList)
                .listSize(blockedMemberDtoList.size())
                .totalPage(blockList.getTotalPages())
                .totalElements(blockList.getTotalElements())
                .isFirst(blockList.isFirst())
                .isLast(blockList.isLast())
                .build();
    }

    public static MemberResponse.blockedMemberDto toBlockedMemberDto(Member membr) {
        return MemberResponse.blockedMemberDto.builder()
                .memberId(membr.getId())
                .profileImg(membr.getProfileImage())
                .email(membr.getEmail())
                .name(membr.getGameuserName())
                .build();
    }
}
