package com.gamegoo.converter;

import com.gamegoo.domain.Friend;
import com.gamegoo.domain.Member;
import com.gamegoo.dto.member.MemberResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

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

    public static MemberResponse.blockedMemberDTO toBlockedMemberDTO(Member member) {
        return MemberResponse.blockedMemberDTO.builder()
            .memberId(member.getId())
            .profileImg(member.getProfileImage())
            .email(member.getEmail())
            .name(member.getGameName())
            .build();

    }

    public static MemberResponse.myProfileMemberDTO toMyProfileDTO(Member member) {
        List<MemberResponse.GameStyleResponseDTO> gameStyleResponseDTOList = null;
        if (member.getMemberGameStyleList() != null) {
            gameStyleResponseDTOList = member.getMemberGameStyleList().stream()
                .map(memberGameStyle -> MemberResponse.GameStyleResponseDTO.builder()
                    .gameStyleId(memberGameStyle.getGameStyle().getId())
                    .gameStyleName(memberGameStyle.getGameStyle().getStyleName())
                    .build()).collect(Collectors.toList());
        }

        List<MemberResponse.ChampionResponseDTO> championResponseDTOList = null;
        if (member.getMemberChampionList() != null) {
            championResponseDTOList = member.getMemberChampionList().stream()
                .map(memberChampion -> MemberResponse.ChampionResponseDTO.builder()
                    .championId(memberChampion.getMember().getId())
                    .championName(memberChampion.getChampion().getName())
                    .build()).collect(Collectors.toList());
        }

        return MemberResponse.myProfileMemberDTO.builder()
            .email(member.getEmail())
            .gameName(member.getGameName())
            .tag(member.getTag())
            .tier(member.getTier())
            .rank(member.getRank())
            .profileImg(member.getProfileImage())
            .updatedAt(String.valueOf(member.getUpdatedAt()))
            .gameStyleResponseDTOList(gameStyleResponseDTOList)
            .championResponseDTOList(championResponseDTOList)
            .build();
    }

    public static MemberResponse.friendInfoDTO toFriendInfoDto(Friend friend) {
        return MemberResponse.friendInfoDTO.builder()
            .memberId(friend.getToMember().getId())
            .name(friend.getToMember().getGameName())
            .memberProfileImg(friend.getToMember().getProfileImage())
            .isLiked(friend.getIsLiked())
            .build();
    }

}
