package com.gamegoo.util;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Member;

public class MemberUtils {

    /**
     * member가 targetMember를 차단한 상태인지 리턴
     *
     * @param member
     * @param targetMember
     * @return
     */
    public static boolean isBocked(Member member, Member targetMember) {

        return member.getBlockList().stream()
            .anyMatch(block -> block.getBlockedMember().equals(targetMember));
    }

    /**
     * 해당 회원이 탈퇴했는지 검증
     *
     * @param member
     */
    public static boolean checkBlind(Member member) {
        if (member.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }
        return false;
    }


}
