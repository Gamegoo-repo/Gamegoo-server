package com.gamegoo.converter;

import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.MemberResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class MemberConverter {

    public static MemberResponse.blockListDTO toBlockListDTO(Page<Member> blockList) {
        List<MemberResponse.blockedMemberDTO> blockedMemberDtoList = blockList.stream()
                .map(MemberConverter::toBlockedMemberDTO)
                .collect(Collectors.toList());

        return MemberResponse.blockListDTO.builder()
                .blockedMemberDTOList(blockedMemberDtoList)
                .listSize(blockedMemberDtoList.size())
                .totalPage(blockList.getTotalPages())
                .totalElements(blockList.getTotalElements())
                .isFirst(blockList.isFirst())
                .isLast(blockList.isLast())
                .build();
    }

    public static MemberResponse.blockedMemberDTO toBlockedMemberDTO(Member membr) {
        return MemberResponse.blockedMemberDTO.builder()
                .memberId(membr.getId())
                .profileImg(membr.getProfileImage())
                .email(membr.getEmail())
                .name(membr.getGameName())
                .build();
    }

    public static MemberResponse.myProfileMemberDTO toMyProfileDTO(Member member) {
        List<MemberResponse.GameStyleResponseDTO> dtoList = member.getMemberGameStyleList().stream().map(memberGameStyle -> MemberResponse.GameStyleResponseDTO.builder()
                .gameStyleId(memberGameStyle.getGameStyle().getId())
                .gameStyleName(memberGameStyle.getGameStyle().getStyleName())
                .build()).collect(Collectors.toList());

        return MemberResponse.myProfileMemberDTO.builder()
                .email(member.getEmail())
                .gameName(member.getGameName())
                .tag(member.getTag())
                .tier(member.getTier())
                .rank(member.getRank())
                .profileImg(member.getProfileImage())
                .updatedAt(String.valueOf(member.getUpdatedAt()))
                .gameStyleResponseDTOList(dtoList)
                .build();
    }

}
