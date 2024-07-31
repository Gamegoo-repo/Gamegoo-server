package com.gamegoo.service.chat;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.ChatHandler;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.dto.chat.ChatResponse.ChatroomEnterDTO;
import com.gamegoo.repository.chat.ChatRepository;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.member.ProfileService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final ProfileService profileService;
    private final MemberRepository memberRepository;
    private final MemberChatroomRepository memberChatroomRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatRepository chatRepository;

    /**
     * 대상 회원과의 채팅방 생성 (게시글 및 친구 목록을 통해 생성)
     *
     * @param request
     * @param memberId
     * @return
     */
    public Chatroom createChatroom(ChatRequest.ChatroomCreateRequest request, Long memberId) {
        Member member = profileService.findMember(memberId);

        // 채팅 대상 회원의 존재 여부 검증
        Member targetMember = memberRepository.findById(request.getTargetMemberId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHAT_TARGET_NOT_FOUND));

        // chatroom 엔티티 생성
        String uuid = UUID.randomUUID().toString();
        Chatroom chatroom = Chatroom.builder()
            .uuid(uuid)
            .startMember(member)
            .build();

        Chatroom savedChatroom = chatroomRepository.save(chatroom);

        // MemberChatroom 엔티티 생성 및 연관관계 매핑
        // 나의 MemberChatroom 엔티티
        MemberChatroom memberChatroom = MemberChatroom.builder()
            .lastViewDate(null)
            .lastJoinDate(null)
            .chatroom(chatroom)
            .build();
        memberChatroom.setMember(member);
        memberChatroomRepository.save(memberChatroom);

        // 상대방의 MemberChatroom 엔티티
        MemberChatroom targetMemberChatroom = MemberChatroom.builder()
            .lastViewDate(null)
            .lastJoinDate(null)
            .chatroom(chatroom)
            .build();
        targetMemberChatroom.setMember(targetMember);
        memberChatroomRepository.save(targetMemberChatroom);

        return savedChatroom;
    }

    /**
     * 매칭을 통해 새 채팅방을 생성
     *
     * @param request
     * @return
     */
    public Chatroom createChatroomByMatch(ChatRequest.ChatroomCreateByMatchRequest request) {
        if (request.getMemberList().size() != 2) {
            throw new ChatHandler(ErrorStatus._BAD_REQUEST);
        }
        Member member1 = profileService.findMember(request.getMemberList().get(0));
        Member member2 = profileService.findMember(request.getMemberList().get(1));

        // chatroom 엔티티 생성
        String uuid = UUID.randomUUID().toString();
        Chatroom chatroom = Chatroom.builder()
            .uuid(uuid)
            .startMember(null)
            .build();

        Chatroom savedChatroom = chatroomRepository.save(chatroom);

        LocalDateTime now = LocalDateTime.now();

        // MemberChatroom 엔티티 생성 및 연관관계 매핑
        // member1의 MemberChatroom 엔티티
        MemberChatroom memberChatroom1 = MemberChatroom.builder()
            .lastViewDate(null)
            .lastJoinDate(now)
            .chatroom(chatroom)
            .build();
        memberChatroom1.setMember(member1);
        memberChatroomRepository.save(memberChatroom1);

        // member2의 MemberChatroom 엔티티
        MemberChatroom memberChatroom2 = MemberChatroom.builder()
            .lastViewDate(null)
            .lastJoinDate(now)
            .chatroom(chatroom)
            .build();
        memberChatroom2.setMember(member2);
        memberChatroomRepository.save(memberChatroom2);

        return savedChatroom;
    }

    /**
     * chatroomUuid에 해당하는 채팅방에 입장 처리: 채팅 상대 회원 정보 조회, 메시지 내역 조회 및 lastViewDate 업데이트
     *
     * @param chatroomUuid
     * @param memberId
     * @return
     */
    @Transactional
    public ChatResponse.ChatroomEnterDTO enterChatroom(String chatroomUuid, Long memberId) {
        // chatroom 엔티티 조회 및 해당 회원의 채팅방이 맞는지 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                memberId, chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        // 해당 회원이 퇴장한 채팅방은 아닌지도 나중에 검증 추가하기

        // 채팅 상대 회원 정보 조회
        Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
            chatroom.getId(), memberId);

        // 최근 메시지 내역 조회
        Slice<Chat> recentChats = chatRepository.findRecentChats(chatroom.getId(),
            memberChatroom.getId());

        // 해당 채팅방의 lastViewDate 업데이트
        memberChatroom.updateLastViewDate(LocalDateTime.now());

        // chatMessageDtoList 생성
        List<ChatResponse.ChatMessageDTO> chatMessageDtoList = recentChats.stream()
            .map(chat -> {
                // ISO 8601 형식의 문자열로 변환
                String createdAtIoString = chat.getCreatedAt()
                    .format(DateTimeFormatter.ISO_DATE_TIME);

                return ChatResponse.ChatMessageDTO.builder()
                    .senderId(chat.getFromMember().getId())
                    .senderName(chat.getFromMember().getGameName())
                    .senderProfileImg(chat.getFromMember().getProfileImage())
                    .message(chat.getContents())
                    .createdAt(createdAtIoString)
                    .timestamp(chat.getTimestamp())
                    .build();
            }).collect(Collectors.toList());

        // ChatMessageListDTO 생성
        ChatResponse.ChatMessageListDTO chatMessageListDTO = ChatResponse.ChatMessageListDTO.builder()
            .chatMessageDtoList(chatMessageDtoList)
            .list_size(chatMessageDtoList.size())
            .has_next(recentChats.hasNext())
            .next_cursor(recentChats.hasNext() ? recentChats.getContent().get(0).getTimestamp()
                : null) // next cursor를 현재 chat list의 가장 오래된 chat의 timestamp로 주기
            .build();

        return ChatroomEnterDTO.builder()
            .memberId(targetMember.getId())
            .gameName(targetMember.getGameName())
            .memberProfileImg(targetMember.getProfileImage())
            .chatMessageList(chatMessageListDTO)
            .build();
    }

    /**
     * 채팅 등록 메소드
     *
     * @param request
     * @param memberId
     * @return
     */
    @Transactional
    public Chat addChat(ChatRequest.ChatCreateRequest request, String chatroomUuid, Long memberId) {
        Member member = profileService.findMember(memberId);

        // 채팅방 조회 및 존재 여부 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        // 해당 채팅방이 회원의 것이 맞는지 검증
        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                memberId, chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

//        // 해당 회원이 이미 나간 채팅방인지 검증
//        if (memberChatroom.getLastJoinDate() == null) {
//            throw new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED);
//        }

        // chat 엔티티 생성
        Chat chat = Chat.builder()
            .contents(request.getMessage())
            .chatroom(chatroom)
            .fromMember(member)
            .build();

        // MemberChatroom의 lastViewDate 업데이트
        Chat savedChat = chatRepository.save(chat);
        updateLastViewDateByAddChat(memberChatroom, savedChat.getCreatedAt());
        //memberChatroom.updateLastViewDate(savedChat.getCreatedAt());

        return savedChat;
    }

    /**
     * memberChatroom의 lastViewDate을 업데이트
     *
     * @param chatroomUuid
     * @param timestamp
     * @param memberId
     */
    @Transactional
    public void readChatMessages(String chatroomUuid, Long timestamp, Long memberId) {
        Member member = profileService.findMember(memberId);

        // chatroom 엔티티 조회 및 해당 회원의 채팅방이 맞는지 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                memberId, chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        if (timestamp == null) { // timestamp 파라미터가 넘어오지 않은 경우, lastViewDate를 현재 시각으로 업데이트
            memberChatroom.updateLastViewDate(LocalDateTime.now());

        } else { // timestamp 파라미터가 넘어온 경우, lastViewDate를 해당 timestamp의 chat의 createdAt으로 업데이트
            Chat chat = chatRepository.findByChatroomAndTimestamp(chatroom, timestamp)
                .orElseThrow(() -> new ChatHandler(ErrorStatus.CHAT_MESSAGE_NOT_FOUND));
            memberChatroom.updateLastViewDate(chat.getCreatedAt());
        }
    }

    /**
     * 해당 채팅방 나가기
     *
     * @param chatroomUuid
     * @param memberId
     */
    @Transactional
    public void exitChatroom(String chatroomUuid, Long memberId) {
        Member member = profileService.findMember(memberId);

        // chatroom 엔티티 조회 및 해당 회원의 채팅방이 맞는지 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                memberId, chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        memberChatroom.updateLastJoinDate(null);

    }

    private void updateLastViewDateByAddChat(MemberChatroom memberChatroom,
        LocalDateTime lastViewDate) {
        // lastJoinDate가 null인 경우
        if (memberChatroom.getLastJoinDate() == null) {
            // lastViewDate 업데이트
            memberChatroom.updateLastViewDate(lastViewDate);

            // lastJoinDate 업데이트
            memberChatroom.updateLastJoinDate(lastViewDate);

        } else {
            // lastViewDate 업데이트
            memberChatroom.updateLastViewDate(lastViewDate);

        }

        // 상대 회원의 memberChatroom의 latJoinDate가 null인 경우, 상대 회원의 lastJoinDate 업데이트
        Chatroom chatroom = memberChatroom.getChatroom();

        Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
            chatroom.getId(), memberChatroom.getMember().getId());
        MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
            targetMember.getId(), chatroom.getId()).get();
        if (targetMemberChatroom.getLastJoinDate() == null) {
            targetMemberChatroom.updateLastJoinDate(lastViewDate);
        }
    }

}
