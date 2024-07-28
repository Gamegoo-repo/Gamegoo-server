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
            .blocked_member_dto_list(blockedMemberDtoList)
            .list_size(blockedMemberDtoList.size())
            .total_page(blockList.getTotalPages())
            .total_elements(blockList.getTotalElements())
            .is_first(blockList.isFirst())
            .is_last(blockList.isLast())
            .build();
    }

    public static MemberResponse.blockedMemberDTO toBlockedMemberDTO(Member membr) {
        return MemberResponse.blockedMemberDTO.builder()
            .member_id(membr.getId())
            .profile_img(membr.getProfileImage())
            .email(membr.getEmail())
            .name(membr.getGameName())
            .build();
    }

    public static MemberResponse.myProfileMemberDTO toMyProfileDTO(Member member) {
        List<MemberResponse.GameStyleResponseDTO> dtoList = member.getMemberGameStyleList().stream()
            .map(memberGameStyle -> MemberResponse.GameStyleResponseDTO.builder()
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

    public static MemberResponse.friendInfoDTO toFriendInfoDto(Friend friend) {
        return MemberResponse.friendInfoDTO.builder()
            .memberId(friend.getToMember().getId())
            .name(friend.getToMember().getGameName())
            .memberProfileImg(friend.getToMember().getProfileImage())
            .isLiked(friend.getIsLiked())
            .build();
    }

}
