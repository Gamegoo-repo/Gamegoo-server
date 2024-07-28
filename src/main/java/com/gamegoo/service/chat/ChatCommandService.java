package com.gamegoo.service.chat;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.ChatHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.enums.ChatroomType;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.member.ProfileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatCommandService {

    private final ProfileService profileService;
    private final MemberRepository memberRepository;
    private final MemberChatroomRepository memberChatroomRepository;
    private final ChatroomRepository chatroomRepository;

    /**
     * 대상 회원과의 채팅방 생성
     *
     * @param request
     * @param memberId
     * @return
     */
    @Transactional
    public Chatroom createChatroom(ChatRequest.ChatroomCreateRequest request, Long memberId) {
        Member member = profileService.findMember(memberId);

        // 채팅 대상 회원의 존재 여부 검증
        Member targetMember = memberRepository.findById(request.getTargetMemberId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHAT_TARGET_NOT_FOUND));

        // chatroom 엔티티 생성
        Chatroom chatroom = null;
        String uuid = UUID.randomUUID().toString();
        if (request.getChatroomType().equals(ChatroomType.FRIEND.toString())) { // 친구목록에서 시작된 채팅인 경우
            chatroom = Chatroom.builder()
                .uuid(uuid)
                .chatroomType(ChatroomType.FRIEND)
                .postUrl(null)
                .startMember(null)
                .build();
        } else { // 특정 글을 보고 시작된 채팅인 경우
            chatroom = Chatroom.builder()
                .uuid(uuid)
                .chatroomType(ChatroomType.POST)
                .postUrl(request.getPostUrl())
                .startMember(member)
                .build();
        }
        Chatroom savedChatroom = chatroomRepository.save(chatroom);

        // MemberChatroom 엔티티 생성 및 연관관계 매핑
        // 나의 MemberChatroom 엔티티
        MemberChatroom memberChatroom = MemberChatroom.builder()
            .lastViewDate(null)
            .chatroom(chatroom)
            .build();
        memberChatroom.setMember(member);
        memberChatroomRepository.save(memberChatroom);

        // 상대방의 MemberChatroom 엔티티
        MemberChatroom targetMemberChatroom = MemberChatroom.builder()
            .lastViewDate(null)
            .chatroom(chatroom)
            .build();
        targetMemberChatroom.setMember(targetMember);
        memberChatroomRepository.save(targetMemberChatroom);

        return savedChatroom;
    }

}
