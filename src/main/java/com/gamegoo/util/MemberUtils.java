package com.gamegoo.util;

import com.gamegoo.domain.Member;

public class BlockUtils {

    /**
     * member가 targetMember를 차단한 상태인지 검증
     *
     * @param member
     * @param targetMember
     * @return
     */
    public static boolean isBocked(Member member, Member targetMember) {

        return member.getBlockList().stream()
            .anyMatch(block -> block.getBlockedMember().equals(targetMember));
    }


}
