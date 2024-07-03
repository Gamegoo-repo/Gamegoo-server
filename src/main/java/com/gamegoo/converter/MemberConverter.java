package com.gamegoo.converter;

import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.MemberResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class MemberConverter {

    public static MemberResponseDTO.blockListDto toBlockListDto(Page<Member> blockList) {
        List<MemberResponseDTO.blockedMemberDto> blockedMemberDtoList = blockList.stream()
                .map(MemberConverter::toBlockedMemberDto)
                .collect(Collectors.toList());

        return MemberResponseDTO.blockListDto.builder()
                .blockedMemberDtoList(blockedMemberDtoList)
                .listSize(blockedMemberDtoList.size())
                .totalPage(blockList.getTotalPages())
                .totalElements(blockList.getTotalElements())
                .isFirst(blockList.isFirst())
                .isLast(blockList.isLast())
                .build();
    }

    public static MemberResponseDTO.blockedMemberDto toBlockedMemberDto(Member membr) {
        return MemberResponseDTO.blockedMemberDto.builder()
                .memberId(membr.getId())
                .profileImg(membr.getProfileImage())
                .email(membr.getEmail())
                .name(membr.getGameuserName())
                .build();
    }
}
