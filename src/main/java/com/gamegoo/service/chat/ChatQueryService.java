package com.gamegoo.service.chat;

import com.gamegoo.domain.Member;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.repository.chat.ChatRepository;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.service.member.ProfileService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
     * @param member
     * @return
     */
    @Transactional(readOnly = true)
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

                String lastAtIoString = null;
                // ISO 8601 형식의 문자열로 변환
                if (lastChat.isPresent()) {
                    lastAtIoString = lastChat.get().getCreatedAt()
                        .format(DateTimeFormatter.ISO_DATE_TIME);
                }

                return ChatResponse.ChatroomViewDTO.builder()
                    .chatroomId(chatroom.getId())
                    .uuid(chatroom.getUuid())
                    .targetMemberImg(targetMember.getProfileImage())
                    .targetMemberName(targetMember.getGameName())
                    .lastMsg(lastChat.isPresent() ? lastChat.get().getContents() : null)
                    .lastMsgAt(lastAtIoString)
                    .notReadMsgCnt(unReadCnt)
                    .build();

            })
            .collect(Collectors.toList());

        return chatroomViewDtoList;

    }


}
