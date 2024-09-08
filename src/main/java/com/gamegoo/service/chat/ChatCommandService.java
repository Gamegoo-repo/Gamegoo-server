package com.gamegoo.service.chat;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.ChatHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.converter.ChatConverter;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatRequest.SystemFlagRequest;
import com.gamegoo.dto.chat.ChatResponse;
import com.gamegoo.dto.chat.ChatResponse.ChatroomEnterDTO;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.chat.ChatRepository;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.service.member.FriendService;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.service.socket.SocketService;
import com.gamegoo.util.MemberUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final ProfileService profileService;
    private final FriendService friendService;
    private final SocketService socketService;
    private final BoardService boardService;
    private final MemberRepository memberRepository;
    private final MemberChatroomRepository memberChatroomRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatRepository chatRepository;
    private final BoardRepository boardRepository;

    private static final String POST_SYSTEM_MESSAGE_TO_MEMBER_INIT = "상대방이 게시한 글을 보고 말을 걸었어요. 대화를 시작해보세요~";
    private static final String POST_SYSTEM_MESSAGE_TO_MEMBER = "상대방이 게시한 글을 보고 말을 걸었어요.";
    private static final String POST_SYSTEM_MESSAGE_TO_TARGET_MEMBER = "내가 게시한 글을 보고 말을 걸어왔어요.";
    private static final String MATCHING_SYSTEM_MESSAGE = "상대방과 매칭이 이루어졌어요!";


    /**
     * 대상 회원과 채팅 시작: 기존에 채팅방이 있는 경우 채팅방 입장 처리, 기존에 채팅방이 없는 경우 새로운 채팅방 생성
     *
     * @param request
     * @param memberId
     * @return
     */
    public ChatResponse.ChatroomEnterDTO startChatroomByMemberId(Long memberId,
        Long targetMemberId) {

        // 대상 회원 검증 및 에러 처리
        validateDifferentMembers(memberId, targetMemberId);

        Member member = profileService.findMember(memberId);

        // 채팅 대상 회원의 존재 여부 검증
        Member targetMember = validateAndGetTargetMember(targetMemberId);

        // 내가 상대 회원 차단했는지 검증
        validateBlockedTargetMember(member, targetMember);

        return chatroomRepository.findChatroomByMemberIds(member.getId(), targetMember.getId())
            .map(existingChatroom -> enterExistingChatroom(member, targetMember, existingChatroom,
                null))// 기존 채팅방 존재하는 경우, 해당 채팅방에 입장
            .orElseGet(() -> {
                // 기존에 채팅방이 존재하지 않는 경우
                // 상대가 나를 차단했는지 검증
                validateBlockedByTargetMember(member, targetMember);

                // 상대가 탈퇴했는지 검증
                validateTargetMemberIsBlind(targetMember,
                    ErrorStatus.CHAT_START_FAILED_TARGET_USER_DEACTIVATED);

                // 새 채팅방 생성
                Chatroom newChatroom = createNewChatroom(member, targetMember, null);

                return ChatroomEnterDTO.builder()
                    .uuid(newChatroom.getUuid())
                    .memberId(targetMember.getId())
                    .gameName(targetMember.getGameName())
                    .memberProfileImg(targetMember.getProfileImage())
                    .friend(friendService.isFriend(member, targetMember))
                    .blocked(false)
                    .blind(targetMember.getBlind())
                    .friendRequestMemberId(
                        friendService.getFriendRequestMemberId(member, targetMember))
                    .system(null)
                    .chatMessageList(initChatMessageListDTO())
                    .build();
            });
    }

    /**
     * 특정 글을 보고 채팅 시작: 기존에 채팅방이 있는 경우 채팅방 입장 처리, 기존에 채팅방이 없는 경우 새로운 채팅방 생성
     *
     * @param request
     * @param memberId
     * @return
     */
    public ChatResponse.ChatroomEnterDTO startChatroomByBoardId(Long memberId, Long boardId) {
        Member member = profileService.findMember(memberId);

        // 게시글 엔티티 조회
        Board board = boardService.findBoard(boardId);

        // 채팅 대상 회원의 존재 여부 검증
        Member targetMember = memberRepository.findById(board.getMember().getId())
            .orElseThrow(
                () -> new ChatHandler(ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_NOT_FOUND));

        // 게시글 작성자가 본인인 경우
        MemberUtils.validateDifferentMembers(member.getId(), targetMember.getId(),
            ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID);

        // 채팅 대상 회원이 탈퇴한 경우
        MemberUtils.checkBlind(targetMember);

        // 내가 채팅 대상 회원을 차단한 경우, 기존 채팅방 입장 및 새 채팅방 생성 모두 불가
        MemberUtils.validateBlocked(member, targetMember,
            ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_IS_BLOCKED);

        return chatroomRepository.findChatroomByMemberIds(
                member.getId(), targetMember.getId())
            .map(exitChatroom -> enterExistingChatroom(member, targetMember,
                exitChatroom, board.getId())) // 기존 채팅방 존재하는 경우, 해당 채팅방에 입장 및 system 값 포함
            .orElseGet(() -> {
                // 기존에 채팅방이 존재하지 않는 경우: 상대방의 차단 여부 검증
                MemberUtils.validateBlocked(targetMember, member,
                    ErrorStatus.CHAT_START_FAILED_BLOCKED_BY_CHAT_TARGET);

                // 새 채팅방 생성
                Chatroom newChatroom = createNewChatroom(member, targetMember, null);

                // 응답 생성
                // 시스템 메시지 기능을 위한 SystemFlagDTO 생성
                ChatResponse.SystemFlagDTO systemFlagDTO = ChatResponse.SystemFlagDTO.builder()
                    .flag(1)
                    .boardId(boardId)
                    .build();

                // chatMessageListDTO 생성
                ChatResponse.ChatMessageListDTO chatMessageListDTO = ChatResponse.ChatMessageListDTO.builder()
                    .chatMessageDtoList(new ArrayList<>())
                    .list_size(0)
                    .has_next(false)
                    .next_cursor(null)
                    .build();

                return ChatroomEnterDTO.builder()
                    .uuid(newChatroom.getUuid())
                    .memberId(targetMember.getId())
                    .gameName(targetMember.getGameName())
                    .memberProfileImg(targetMember.getProfileImage())
                    .friend(friendService.isFriend(member, targetMember))
                    .blocked(false)
                    .blind(targetMember.getBlind())
                    .friendRequestMemberId(
                        friendService.getFriendRequestMemberId(member, targetMember))
                    .system(systemFlagDTO)
                    .chatMessageList(null)
                    .build();
            });
    }

    /**
     * 매칭 성공 시 채팅방 시작
     *
     * @param request
     * @return
     */
    public String startChatroomByMatching(Long memberId1, Long memberId2) {

        // 매칭 대상 회원이 동일한 회원인 경우
        MemberUtils.validateDifferentMembers(memberId1, memberId2,
            ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID);

        Member member1 = profileService.findMember(memberId1);
        Member member2 = profileService.findMember(memberId2);

        // 대상 회원의 탈퇴 여부 검증
        if (member2.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }

        // 내가 상대를 차단한 경우
        if (MemberUtils.isBlocked(member1, member2)) {
            throw new ChatHandler(ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_IS_BLOCKED);
        }

        // 상대가 나를 차단한 경우
        if (MemberUtils.isBlocked(member2, member1)) {
            throw new ChatHandler(ErrorStatus.CHAT_START_FAILED_BLOCKED_BY_CHAT_TARGET);
        }

        Chatroom chatroom = chatroomRepository
            .findChatroomByMemberIds(member1.getId(), member2.getId())
            .map(existingChatroom -> updateLastJoinDateWithOutSocket(member1, member2,
                existingChatroom,
                LocalDateTime.now())) // 기존 채팅방 존재하는 경우, 서로의 lastJoinDate가 null이면 현재 시각으로 업데이트
            .orElseGet(() -> createNewChatroom(member1, member2,
                LocalDateTime.now())); // 기존 채팅방 존재하지 않는 경우, 새로운 채팅방 생성

        // 두 회원에게 매칭 시스템 메시지 생성 및 저장
        createAndSaveSystemChat(chatroom, member1, MATCHING_SYSTEM_MESSAGE, null);
        createAndSaveSystemChat(chatroom, member2, MATCHING_SYSTEM_MESSAGE, null);

        return chatroom.getUuid();
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
        Member member = profileService.findMember(memberId);

        // chatroom 엔티티 조회 및 해당 회원의 채팅방이 맞는지 검증
        Chatroom chatroom = chatroomRepository.findByUuid(chatroomUuid)
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_EXIST));

        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                memberId, chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        // 채팅 상대 회원 조회
        Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
            chatroom.getId(), memberId);

        // 내가 채팅 상대 회원을 차단한 경우
        MemberUtils.validateBlocked(member, targetMember,
            ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_IS_BLOCKED);

        return enterExistingChatroom(member, targetMember, chatroom, null);
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

        // 회원 간 차단 여부 및 탈퇴 여부 검증
        // 대화 상대 회원 조회
        Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
            chatroom.getId(), memberId);
        // 상대 탈퇴 여부 검증
        if (targetMember.getBlind()) {
            throw new MemberHandler(ErrorStatus.USER_DEACTIVATED);
        }
        MemberUtils.validateBlocked(member, targetMember,
            ErrorStatus.CHAT_TARGET_IS_BLOCKED_SEND_CHAT_FAILED);
        MemberUtils.validateBlocked(targetMember, member,
            ErrorStatus.BLOCKED_BY_CHAT_TARGET_SEND_CHAT_FAILED);

        // 등록해야 할 시스템 메시지가 있는 경우
        if (request.getSystem() != null) {
            SystemFlagRequest systemFlag = request.getSystem();
            Optional<Board> board = boardRepository.findById(systemFlag.getBoardId());

            // member 대상 시스템 메시지 생성
            String messageContent =
                systemFlag.getFlag().equals(1) ? POST_SYSTEM_MESSAGE_TO_MEMBER_INIT
                    : POST_SYSTEM_MESSAGE_TO_MEMBER;

            Chat systemChatToMember = createAndSaveSystemChat(chatroom, member, messageContent,
                board.orElse(null));

            // targetMember 대상 시스템 메시지 생성
            Chat systemChatToTargetMember = createAndSaveSystemChat(chatroom, targetMember,
                POST_SYSTEM_MESSAGE_TO_TARGET_MEMBER,
                board.orElse(null));

            updateLastJoinDateBySystemChat(memberChatroom, systemChatToMember.getCreatedAt(),
                systemChatToTargetMember.getCreatedAt());

        }

        // chat 엔티티 생성
        Chat chat = Chat.builder()
            .contents(request.getMessage())
            .chatroom(chatroom)
            .fromMember(member)
            .build();

        // MemberChatroom의 lastViewDate 업데이트
        Chat savedChat = chatRepository.save(chat);
        if (request.getSystem() == null) {
            updateLastViewDateByAddChat(memberChatroom, savedChat.getCreatedAt());
        } else {
            memberChatroom.updateLastViewDate(savedChat.getCreatedAt());
        }

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
                member.getId(), chatroom.getId())
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
                member.getId(), chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        memberChatroom.updateLastJoinDate(null);

    }


    /* private 메소드 */

    /**
     * 두 회원의 lastJoinDate가 각각 null인 경우, 해당 lastJoinDate로 업데이트. socket API 호출 미포함
     *
     * @param member1
     * @param member2
     * @param chatroom
     * @param lastJoinDate
     * @return
     */
    private Chatroom updateLastJoinDateWithOutSocket(Member member1, Member member2,
        Chatroom chatroom, LocalDateTime lastJoinDate) {

        updateLastJoinDateIfNull(member1, chatroom, lastJoinDate);
        updateLastJoinDateIfNull(member2, chatroom, lastJoinDate);
        return chatroom;
    }

    /**
     * member의 lastJoinDate가 null이 아닌 경우, lastJoinDate 업데이트
     *
     * @param member
     * @param chatroom
     * @param lastJoinDate
     */
    private void updateLastJoinDateIfNull(Member member, Chatroom chatroom,
        LocalDateTime lastJoinDate) {
        MemberChatroom memberChatroom = memberChatroomRepository
            .findByMemberIdAndChatroomId(member.getId(), chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        if (memberChatroom.getLastJoinDate() == null) {
            memberChatroom.updateLastJoinDate(lastJoinDate);
        }
    }


    /**
     * 채팅 등록 시 나와 상대방의 lastViewDate 업데이트
     *
     * @param memberChatroom
     * @param lastViewDate
     */
    private void updateLastViewDateByAddChat(MemberChatroom memberChatroom,
        LocalDateTime lastViewDate) {
        // lastJoinDate가 null인 경우
        if (memberChatroom.getLastJoinDate() == null) {
            // lastViewDate 업데이트
            memberChatroom.updateLastViewDate(lastViewDate);

            // lastJoinDate 업데이트
            memberChatroom.updateLastJoinDate(lastViewDate);

            // lastJoinDate 업데이트로 인해 socket room join API 요청
            socketService.joinSocketToChatroom(memberChatroom.getMember().getId(),
                memberChatroom.getChatroom().getUuid());

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

            // lastJoinDate 업데이트로 인해 socket room join API 요청
            socketService.joinSocketToChatroom(targetMember.getId(),
                targetMemberChatroom.getChatroom().getUuid());
        }
    }

    /**
     * 시스템 메시지 등록 시 나와 상대방의 lastJoinDate 업데이트
     *
     * @param memberChatroom
     * @param memberSystemChatCreatedAt
     * @param targetSystemChatCreatedAt
     */
    private void updateLastJoinDateBySystemChat(MemberChatroom memberChatroom,
        LocalDateTime memberSystemChatCreatedAt, LocalDateTime targetSystemChatCreatedAt
    ) {
        // lastJoinDate가 null인 경우
        if (memberChatroom.getLastJoinDate() == null) {
            // lastJoinDate 업데이트
            memberChatroom.updateLastJoinDate(memberSystemChatCreatedAt);

            // lastJoinDate 업데이트로 인해 socket room join API 요청
            socketService.joinSocketToChatroom(memberChatroom.getMember().getId(),
                memberChatroom.getChatroom().getUuid());

        }

        // 상대 회원의 memberChatroom의 latJoinDate가 null인 경우, 상대 회원의 lastJoinDate 업데이트
        Chatroom chatroom = memberChatroom.getChatroom();

        Member targetMember = memberChatroomRepository.findTargetMemberByChatroomIdAndMemberId(
            chatroom.getId(), memberChatroom.getMember().getId());
        MemberChatroom targetMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
            targetMember.getId(), chatroom.getId()).get();
        if (targetMemberChatroom.getLastJoinDate() == null) {
            targetMemberChatroom.updateLastJoinDate(targetSystemChatCreatedAt);

            // lastJoinDate 업데이트로 인해 socket room join API 요청
            socketService.joinSocketToChatroom(targetMember.getId(),
                targetMemberChatroom.getChatroom().getUuid());
        }
    }

    /**
     * 두 회원 간 새로운 채팅방 생성
     *
     * @param member1
     * @param member2
     * @return
     */
    private Chatroom createNewChatroom(Member member1, Member member2, LocalDateTime lastJoinDate) {
        String uuid = UUID.randomUUID().toString();
        Chatroom newChatroom = Chatroom.builder()
            .uuid(uuid)
            .startMember(null)
            .build();

        chatroomRepository.save(newChatroom);

        createAndSaveMemberChatroom(member1, newChatroom, lastJoinDate);
        createAndSaveMemberChatroom(member2, newChatroom, lastJoinDate);

        return newChatroom;
    }


    /**
     * 해댕 회원 및 채팅방에 대한 MemberChatroom 엔티티 생성 및 저장
     *
     * @param member
     * @param chatroom
     * @param lastJoinDate
     */
    private void createAndSaveMemberChatroom(Member member, Chatroom chatroom,
        LocalDateTime lastJoinDate) {
        MemberChatroom memberChatroom = MemberChatroom.builder()
            .lastViewDate(null)
            .lastJoinDate(lastJoinDate)
            .chatroom(chatroom)
            .build();
        memberChatroom.setMember(member);
        memberChatroomRepository.save(memberChatroom);
    }

    /**
     * 시스템 메시지 생성 및 저장
     *
     * @param chatroom
     * @param toMember
     * @param content
     * @param sourceBoard
     * @return
     */
    private Chat createAndSaveSystemChat(Chatroom chatroom, Member toMember,
        String content, Board sourceBoard) {
        Member systemMember = profileService.findSystemMember();

        Chat systemChat = Chat.builder()
            .contents(content)
            .chatroom(chatroom)
            .fromMember(systemMember)
            .toMember(toMember)
            .sourceBoard(sourceBoard)
            .build();

        return chatRepository.save(systemChat);
    }

    /**
     * member를 해당 chatroom에 입장 처리
     *
     * @param member
     * @param targetMember
     * @param chatroom
     * @return
     */
    private ChatResponse.ChatroomEnterDTO enterExistingChatroom(Member member, Member targetMember,
        Chatroom chatroom, Long boardId) {
        MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                member.getId(), chatroom.getId())
            .orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_ACCESS_DENIED));

        // 내가 퇴장 상태인 경우 검증
        if (memberChatroom.getLastJoinDate() == null) {
            // 상대방이 나를 차단했는지 검증
            validateBlockedByTargetMember(member, targetMember);

            // 상대방이 탈퇴했는지 검증
            validateTargetMemberIsBlind(targetMember,
                ErrorStatus.CHAT_START_FAILED_BLOCKED_BY_CHAT_TARGET);
        }

        // 최근 메시지 내역 조회
        Slice<Chat> recentChats = chatRepository.findRecentChats(chatroom.getId(),
            memberChatroom.getId(), member.getId());

        // lastViewDate 업데이트
        memberChatroom.updateLastViewDate(LocalDateTime.now());

        // ChatMessageListDTO 생성
        ChatResponse.ChatMessageListDTO chatMessageListDTO = ChatConverter.toChatMessageListDTO(
            recentChats);

        ChatResponse.SystemFlagDTO systemFlagDTO;
        // 시스템 플래그 생성, boardId가 null인 경우 systemFlagDTO도 null
        // 상대가 나를 차단한 경우 boardId null을 전달해 systemFlagDTO null로 설정
        if (MemberUtils.isBlocked(targetMember, member)) {
            systemFlagDTO = createSystemFlagDTO(memberChatroom, null);
        } else {
            systemFlagDTO = createSystemFlagDTO(memberChatroom, boardId);
        }

        return ChatroomEnterDTO.builder()
            .uuid(chatroom.getUuid())
            .memberId(targetMember.getId())
            .gameName(targetMember.getBlind() ? "(탈퇴한 사용자)" : targetMember.getGameName())
            .memberProfileImg(targetMember.getProfileImage())
            .friend(friendService.isFriend(member, targetMember))
            .blocked(MemberUtils.isBlocked(targetMember, member))
            .blind(targetMember.getBlind())
            .friendRequestMemberId(friendService.getFriendRequestMemberId(member, targetMember))
            .system(systemFlagDTO)
            .chatMessageList(chatMessageListDTO)
            .build();
    }

    /**
     * boardId 값에 따른 systemFlag dto 생성
     *
     * @param memberChatroom
     * @param boardId
     * @return
     */
    private ChatResponse.SystemFlagDTO createSystemFlagDTO(MemberChatroom memberChatroom,
        Long boardId) {
        if (boardId == null) {
            return null;
        }
        return memberChatroom.getLastJoinDate() == null
            ? ChatResponse.SystemFlagDTO.builder().flag(1).boardId(boardId).build()
            : ChatResponse.SystemFlagDTO.builder().flag(2).boardId(boardId).build();
    }

    /**
     * ChatMessageListDTO 객체를 초기화해 리턴
     *
     * @return
     */
    private ChatResponse.ChatMessageListDTO initChatMessageListDTO() {
        return ChatResponse.ChatMessageListDTO.builder()
            .chatMessageDtoList(new ArrayList<>())
            .list_size(0)
            .has_next(false)
            .next_cursor(null)
            .build();
    }

    //--- 검증 메소드 ---//

    private void validateDifferentMembers(Long member1, Long member2) {
        if (member1.equals(member2)) {
            throw new ChatHandler(ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID);
        }
    }

    private Member validateAndGetTargetMember(Long targetMemberId) {
        return memberRepository.findById(targetMemberId)
            .orElseThrow(
                () -> new ChatHandler(ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_NOT_FOUND));

    }

    private void validateBlockedTargetMember(Member member, Member targetMember) {
        if (MemberUtils.isBlocked(member, targetMember)) {
            throw new ChatHandler(ErrorStatus.CHAT_START_FAILED_CHAT_TARGET_IS_BLOCKED);
        }
    }

    private void validateBlockedByTargetMember(Member member, Member targetMember) {
        if (MemberUtils.isBlocked(targetMember, member)) {
            throw new ChatHandler(ErrorStatus.CHAT_START_FAILED_BLOCKED_BY_CHAT_TARGET);
        }
    }

    private void validateTargetMemberIsBlind(Member targetMember, ErrorStatus errorStatus) {
        if (targetMember.getBlind()) {
            throw new ChatHandler(errorStatus);
        }
    }
}
