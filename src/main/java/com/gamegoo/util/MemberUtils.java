package com.gamegoo.util;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Block;
import com.gamegoo.domain.member.Member;

public class MemberUtils {

    /**
     * member가 targetMember를 차단한 상태인지 리턴
     *
     * @param member
     * @param targetMember
     * @return
     */
    public static boolean isBlocked(Member member, Member targetMember) {

        return member.getBlockList().stream()
            .anyMatch(block -> block.getBlockedMember().equals(targetMember));
    }

    /**
     * member가 targetMember를 차단한 상태이면, 해당 ErrorStatus로 에러 처리
     *
     * @param member
     * @param targetMember
     * @param errorStatus
     */
    public static void validateBlocked(Member member, Member targetMember,
        ErrorStatus errorStatus) {
        for (Block block : member.getBlockList()) {
            if (block.getBlockedMember().equals(targetMember)) {
                throw new MemberHandler(errorStatus);
            }
        }
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

    /**
     * 두 회원이 서로 다른 회원인지 검증 및 검증 실패 시 에러 처리
     *
     * @param memberId1
     * @param memberId2
     * @param errorStatus
     */
    public static void validateDifferentMembers(Long memberId1, Long memberId2,
        ErrorStatus errorStatus) {
        if (memberId1.equals(memberId2)) {
            throw new MemberHandler(errorStatus);
        }
    }


}
