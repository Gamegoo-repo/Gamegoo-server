package com.gamegoo.service.member;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BlockHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.PageHandler;
import com.gamegoo.domain.Block;
import com.gamegoo.domain.member.Member;
import com.gamegoo.repository.member.BlockRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.chat.ChatQueryService;
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
    private final ProfileService profileService;
    private final ChatQueryService chatQueryService;
    private final ChatCommandService chatCommandService;
    private final FriendService friendService;

    private final static Integer PAGE_SIZE = 10;

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원을 차단
     *
     * @param memberId
     * @param targetMemberId
     * @return
     */
    public Member blockMember(Long memberId, Long targetMemberId) {
        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

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
            .isDeleted(false)
            .blockedMember(targetMember)
            .build();
        block.setBlockerMember(member);

        blockRepository.save(block);

        // 차단 대상 회원과의 채팅방이 존재하는 경우, 해당 채팅방 퇴장 처리
        chatQueryService.getChatroomByMembers(member, targetMember).ifPresent(chatroom -> {
            // 채팅방 퇴장 처리
            chatCommandService.exitChatroom(chatroom.getUuid(), member.getId());
        });

        // 차단 대상 회원과 친구관계인 경우, 친구 관계 끊기
        friendService.removeFriendshipIfPresent(member, targetMember);

        // 차단 대상 회원에게 보냈던 친구 요청이 있는 경우, 해당 요청 취소 처리
        friendService.cancelPendingFriendRequests(member, targetMember);

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
        Member member = profileService.findMember(memberId);

        PageRequest pageRequest = PageRequest.of(pageIdx, PAGE_SIZE);

        return memberRepository.findBlockedMembersByBlockerIdAndNotDeleted(member.getId(),
            pageRequest);
    }

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원에 대한 차단을 해제
     *
     * @param memberId
     * @param targetMemberId
     */
    public void unBlockMember(Long memberId, Long targetMemberId) {
        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

        // 대상 회원이 탈퇴 상태가 아닌지 검증
        if (targetMember.getBlind()) {
            throw new BlockHandler(ErrorStatus.UNBLOCK_TARGET_MEMBER_BLIND);
        }

        // targetMember가 차단 실제로 차단 목록에 존재하는지 검증
        Block block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember)
            .orElseThrow(() -> new BlockHandler(ErrorStatus.TARGET_MEMBER_NOT_BLOCKED));

        block.removeBlockerMember(member); // 양방향 연관관계 제거
        blockRepository.delete(block);
    }

    /**
     * memberId에 해당하는 회원이 targetMemberId에 해당하는 회원을 자신의 차단 목록에서 삭제 처리 =
     *
     * @param memberId
     * @param targetMemberId
     */
    public void deleteBlockMember(Long memberId, Long targetMemberId) {
        // member 엔티티 조회
        Member member = profileService.findMember(memberId);
        Member targetMember = profileService.findMember(targetMemberId);

        // targetMember가 차단 실제로 차단 목록에 존재하는지 검증
        Block block = blockRepository.findByBlockerMemberAndBlockedMember(member, targetMember)
            .orElseThrow(() -> new BlockHandler(ErrorStatus.TARGET_MEMBER_NOT_BLOCKED));

        // targetMember가 탈퇴한 회원이 맞는지 검증
        if (!targetMember.getBlind()) {
            throw new BlockHandler(ErrorStatus.DELETE_BLOCKED_MEMBER_FAILED);
        }

        // Block 엔티티의 isDeleted 업데이트
        block.updateIsDeleted(true);

    }


}
