package com.gamegoo.service.chat;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.ChatHandler;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.repository.chat.ChatRepository;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.util.DatetimeUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final ChatroomRepository chatroomRepository;
    private final MemberChatroomRepository memberChatroomRepository;
    private final ChatRepository chatRepository;
    private final ProfileService profileService;
    private final static int PAGE_SIZE = 20;


    /**
     * 해당 회원의 ACTIVE한 채팅방의 uuid list를 리턴
     *
     * @param memberId
     * @return
     */
    public List<String> getChatroomUuids(Long memberId) {
        return chatroomRepository.findActiveChatroomUuidsByMemberId(memberId);
    }

    /**
     * 채팅방 목록 조회
     *
     * @param memberId
     * @return
     */
    public List<ChatResponse.ChatroomViewDTO> getChatroomList(Long memberId) {
        Member member = profileService.findMember(memberId);

        // 현재 참여중인 memberChatroom을 각 memberChatroom에 속한 chat의 마지막 createdAt 기준 desc 정렬해 조회
        List<MemberChatroom> activeMemberChatroom = memberChatroomRepository.findActiveMemberChatroomOrderByLastChat(
                member.getId());

        List<ChatResponse.ChatroomViewDTO> chatroomViewDtoList = activeMemberChatroom.stream()
                .map(memberChatroom -> {
                    // 채팅 상대 회원 조회
                    Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
                            memberChatroom.getChatroom().getId(), member.getId());
                    Chatroom chatroom = memberChatroom.getChatroom();

                    // 가장 마지막 대화 조회
                    Optional<Chat> lastChat = chatRepository.findFirstByChatroomIdOrderByCreatedAtDesc(
                            chatroom.getId());

                    // 내가 읽지 않은 메시지 개수 조회
                    Integer unReadCnt = chatRepository.countUnreadChats(
                            chatroom.getId(), memberChatroom.getId());

                    return ChatResponse.ChatroomViewDTO.builder()
                            .chatroomId(chatroom.getId())
                            .uuid(chatroom.getUuid())
                            .targetMemberImg(targetMember.getProfileImage())
                            .targetMemberName(targetMember.getGameName())
                            .lastMsg(lastChat.isPresent() ? lastChat.get().getContents() : null)
                            .lastMsgAt(lastChat.isPresent() ? DatetimeUtil.toKSTString(
                                    lastChat.get().getCreatedAt())
                                    : DatetimeUtil.toKSTString(memberChatroom.getLastJoinDate()))
                            .notReadMsgCnt(unReadCnt)
                            .build();

                })
                .collect(Collectors.toList());

        return chatroomViewDtoList;

    }

    /**
     * chatroomUuid에 해당하는 채팅방의 메시지 내역 조회, 페이징 포함
     *
     * @param chatroomUuid
     * @param memberId
     * @param cursor
     * @return
     */
    public Slice<Chat> getChatMessagesByCursor(String chatroomUuid, Long memberId, Long cursor) {
        Member member = profileService.findMember(memberId);

        // chatroom 엔티티 조회 및 해당 회원의 채팅방이 맞는지 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
                .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                        member.getId(), chatroom.getId())
                .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        // 해당 회원이 퇴장한 채팅방은 아닌지도 나중에 검증 추가하기

        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // requestParam으로 cursor가 넘어온 경우
        if (cursor != null) {
            return chatRepository.findChatsByCursor(cursor, chatroom.getId(),
                    memberChatroom.getId(), pageRequest);
        } else { // cursor가 넘어오지 않은 경우 = 해당 chatroom의 가장 최근 chat을 조회하는 요청
            return chatRepository.findRecentChats(chatroom.getId(), memberChatroom.getId());
        }
    }

    /**
     * 두 회원 간의 Chatroom 엔티티 반환
     *
     * @param member1
     * @param member2
     * @return
     */
    public Optional<Chatroom> getChatroomByMembers(Member member1, Member member2) {
        return chatroomRepository.findChatroomByMemberIds(member1.getId(), member2.getId());
    }


}
