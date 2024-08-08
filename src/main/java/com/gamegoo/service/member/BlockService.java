package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BlockHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.PageHandler;
import com.gamegoo.domain.Block;
import com.gamegoo.domain.Member;
import com.gamegoo.repository.member.BlockRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final MemberRepository memberRepository;
    private final BlockRepository blockRepository;

    Integer pageSize = 9;

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원을 차단
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    public Member blockMember(Long memberId, Long targetMemberId) {
        // member에 대한 검증
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));

        // 본인이 본인을 차단 시도하는 경우
        if (member.equals(targetMember)) {
            throw new MemberHandler(ErrorStatus.BLOCK_MEMBER_BAD_REQUEST);
        }

        // 대상 회원의 탈퇴 여부 검증
        MemberUtils.checkBlind(targetMember);

        // 이미 차단한 회원인지 검증
        boolean isblocked = blockRepository.existsByBlockerMemberAndBlockedMember(member,
            targetMember);
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

    /**
     * memberId에 해당하는 회원이 차단한 회원 목록 조회
     *
     * @param memberId
     * @param pageIdx  0 이상의 값이어야 함
     * @return
     */
    @Transactional(readOnly = true)
    public Page<Member> getBlockList(Long memberId, Integer pageIdx) {
        // 페이지 값 검증
        if (pageIdx < 0) {
            throw new PageHandler(ErrorStatus.PAGE_INVALID);
        }

        // member 엔티티 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        PageRequest pageRequest = PageRequest.of(pageIdx, pageSize);

        return memberRepository.findBlockedMembersByBlockerIdAndNotBlind(member.getId(),
            pageRequest);
    }

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원에 대한 차단을 해제
     *
     * @param memberId
     * @param targetMemberId
     */
    public void unBlockMember(Long memberId, Long targetMemberId) {
        // member에 대한 검증
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member targetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus.TARGET_MEMBER_NOT_FOUND));

        // targetMember가 차단 실제로 차단 목록에 존재하는지 검증
        Block block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember)
            .orElseThrow(() -> new BlockHandler(ErrorStatus.TARGET_MEMBER_NOT_BLOCKED));

        block.removeBlockerMember(member); // 양방향 연관관계 제거
        blockRepository.delete(block);
    }


}