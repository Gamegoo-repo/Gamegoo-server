package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BlockHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Block;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.BlockRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final BlockRepository blockRepository;

    Integer pageSize = 10;

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원을 차단
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    public Member blockMember(Long memberId, Long targetMemberId) {
        // member에 대한 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetMemberId).orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));

        // 대상 회원의 탈퇴 여부 검증
        checkBlind(targetMember);

        // 이미 차단한 회원인지 검증
        boolean isblocked = blockRepository.existsByBlockerMemberAndBlockedMember(member, targetMember);
        if (isblocked) {
            throw new BlockHandler(ErrorStatus.ALREADY_BLOCKED);
        }

        // block 엔티티 생성 및 연관관계 매핑
        Block block = Block.builder()
                .blockedMember(targetMember)
                .build();
        block.setBlockerMember(member);

        blockRepository.save(block);

        return member;
    }

    public Page<Member> getBlockList(Long memberId, Integer pageIdx) {
        // member 엔티티 조회
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        PageRequest pageRequest = PageRequest.of(pageIdx, pageSize);

        return memberRepository.findBlockedMembersByBlockerIdAndNotBlind(member.getId(), pageRequest);
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
