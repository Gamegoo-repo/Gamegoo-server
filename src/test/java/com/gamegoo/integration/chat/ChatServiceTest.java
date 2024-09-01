package com.gamegoo.integration.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.GeneralException;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.member.LoginType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.chat.ChatRequest;
import com.gamegoo.dto.chat.ChatRequest.ChatCreateRequest;
import com.gamegoo.dto.chat.ChatRequest.SystemFlagRequest;
import com.gamegoo.dto.chat.ChatResponse.ChatroomEnterDTO;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.chat.ChatRepository;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.chat.ChatCommandService;
import com.gamegoo.service.member.BlockService;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.service.socket.SocketService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@Transactional
public class ChatServiceTest {

    @Autowired
    private ChatCommandService chatCommandService;

    @Autowired
    private BlockService blockService;

    @SpyBean
    private ProfileService profileService;  // 실제 ProfileService를 Spy로 감싸기

    @SpyBean
    private SocketService socketService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    @Autowired
    private BoardRepository boardRepository;

    private Member member1;

    private Member member2;

    private Member member3;

    private Member blindMember;

    private Member systemMember;

    private Board member2Board;

    private static final String POST_SYSTEM_MESSAGE_TO_MEMBER_INIT = "상대방이 게시한 글을 보고 말을 걸었어요. 대화를 시작해보세요~";
    private static final String POST_SYSTEM_MESSAGE_TO_MEMBER = "상대방이 게시한 글을 보고 말을 걸었어요.";
    private static final String POST_SYSTEM_MESSAGE_TO_TARGET_MEMBER = "내가 게시한 글을 보고 말을 걸어왔어요.";
    private static final String MATCHING_SYSTEM_MESSAGE = "상대방과 매칭이 이루어졌어요!";

    @BeforeEach
    public void setMember() {
        // 기본 테스트에 사용할 멤버 객체를 미리 생성
        int randomProfileImage = ThreadLocalRandom.current().nextInt(1, 9);
        member1 = Member.builder()
            .id(1L)
            .email("test@mail.com")
            .password("12345678")
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(false)
            .mike(false)
            .mannerLevel(1)
            .isAgree(true)
            .blockList(new ArrayList<>())
            .memberChatroomList(new ArrayList<>())
            .boardList(new ArrayList<>())
            .build();

        member2 = Member.builder()
            .id(2L)
            .email("test2@mail.com")
            .password("12345678")
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(false)
            .mike(false)
            .mannerLevel(1)
            .isAgree(true)
            .blockList(new ArrayList<>())
            .memberChatroomList(new ArrayList<>())
            .boardList(new ArrayList<>())
            .build();

        member3 = Member.builder()
            .id(3L)
            .email("test3@mail.com")
            .password("12345678")
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(false)
            .mike(false)
            .mannerLevel(1)
            .isAgree(true)
            .blockList(new ArrayList<>())
            .memberChatroomList(new ArrayList<>())
            .boardList(new ArrayList<>())
            .build();

        blindMember = Member.builder()
            .id(1000L)
            .email("blind@mail.com")
            .password("12345678")
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(true)
            .mike(false)
            .mannerLevel(1)
            .isAgree(true)
            .blockList(new ArrayList<>())
            .memberChatroomList(new ArrayList<>())
            .build();

        systemMember = Member.builder()
            .id(0L)
            .email("system@mail.com")
            .password("12345678")
            .loginType(LoginType.GENERAL)
            .profileImage(randomProfileImage)
            .blind(false)
            .mike(false)
            .mannerLevel(1)
            .isAgree(true)
            .blockList(new ArrayList<>())
            .memberChatroomList(new ArrayList<>())
            .build();

        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);
        member3 = memberRepository.save(this.member3);
        blindMember = memberRepository.save(blindMember);
        systemMember = memberRepository.save(systemMember);

        member2Board = Board.builder()
            .mode(1)
            .mainPosition(1)
            .subPosition(1)
            .wantPosition(1)
            .mike(true)
            .boardGameStyles(new ArrayList<>())
            .content("content")
            .boardProfileImage(1)
            .build();
        member2Board.setMember(member2);
        member2Board = boardRepository.save(member2Board);

        // findSystemMember 메서드만 mock 처리
        doReturn(systemMember).when(profileService).findSystemMember();
        doNothing().when(socketService).joinSocketToChatroom(anyLong(), anyString());
    }

    @Nested
    @DisplayName("특정 회원과 채팅 시작")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StartChatroomByMemberId {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(1)
            @DisplayName("1. 기존 채팅방 없는 경우")
            public void startChatroomByMemberIdSucceedsWhenNoExistingChatroom() throws Exception {
                // when
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(chatroomEnterDTO);
                assertEquals(member2.getId(), chatroomEnterDTO.getMemberId());

                // 2. 데이터베이스에서 채팅방이 실제로 생성되었는지 검증
                Optional<Chatroom> createdChatroom = chatroomRepository.findByUuid(
                    chatroomEnterDTO.getUuid());
                assertTrue(createdChatroom.isPresent());

                // 3. MemberChatroom 엔티티가 각 회원에 대해 잘 생성되었는지 검증
                Optional<MemberChatroom> member1Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), createdChatroom.get().getId());
                Optional<MemberChatroom> member2Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member2.getId(), createdChatroom.get().getId());

                assertTrue(member1Chatroom.isPresent());
                assertTrue(member2Chatroom.isPresent());
            }

            @Test
            @Order(2)
            @DisplayName("2. 기존 채팅방 있는 경우")
            public void startChatroomByMemberIdSucceedsWhenExistingChatroom() throws Exception {
                //given
                ChatroomEnterDTO beforeChatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                // when
                ChatroomEnterDTO afterChatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(afterChatroomEnterDTO);
                assertEquals(member2.getId(), afterChatroomEnterDTO.getMemberId());

                // 2. 기존에 존재하던 채팅방에 입장된 것인지 검증
                assertTrue(
                    beforeChatroomEnterDTO.getUuid().equals(afterChatroomEnterDTO.getUuid()));

                // 3. chatroomRepository에 1개의 데이터만 있는지 검증
                assertTrue(chatroomRepository.count() == 1);

            }

            @Test
            @Order(3)
            @DisplayName("3. 기존 채팅방 있음 && 상대방이 나를 차단 && 이미 입장한 채팅방인 경우")
            public void startChatroomByMemberIdSucceedsWhenExistChatroomAndBlockedAndEntered()
                throws Exception {
                // given
                // 기존 채팅방 생성
                ChatroomEnterDTO oldChatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                Chatroom beforeChatroom = chatroomRepository.findByUuid(
                    oldChatroomEnterDTO.getUuid()).get();

                MemberChatroom beforeMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), beforeChatroom.getId()).get();

                // 해당 채팅방 입장 처리
                beforeMemberChatroom.updateLastJoinDate(LocalDateTime.now());
                beforeMemberChatroom.updateLastViewDate(LocalDateTime.now());

                LocalDateTime beforeLastViewDate = beforeMemberChatroom.getLastViewDate();

                // 상대방이 나를 차단
                Member blockerMember = blockService.blockMember(member2.getId(), member1.getId());

                // when
                ChatroomEnterDTO newChatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(newChatroomEnterDTO);
                assertEquals(member2.getId(), newChatroomEnterDTO.getMemberId());
                assertEquals(newChatroomEnterDTO.getBlocked(), true);

                // 2. MemberChatroom 엔티티가 잘 업데이트 되었는지 확인
                MemberChatroom afterMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                        member1.getId(), beforeChatroom.getId())
                    .orElseThrow(() -> new IllegalStateException("MemberChatroom not found"));

                // 3. MemberChatroom의 lastViewDate가 업데이트 되었는지 검증
                assertTrue(beforeLastViewDate.isBefore(afterMemberChatroom.getLastViewDate()));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(4)
            @DisplayName("4. 동일한 회원 id로 요청한 경우")
            public void startChatroomByMemberIdFailsWhenMemberIdsAreSame() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member1.getId(), member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());
            }

            @Test
            @Order(5)
            @DisplayName("5. 채팅 대상 회원이 존재하지 않는 경우")
            public void startChatroomByMemberIdFailsWhenMemberNotExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_NOT_FOUND;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member1.getId(), 10L);
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());
            }

            @Test
            @Order(6)
            @DisplayName("6. 채팅 대상 회원이 탈퇴한 경우")
            public void startChatroomByMemberIdFailsWhenTargetMemberIsBlind() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.USER_DEACTIVATED;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member1.getId(),
                        blindMember.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());
            }

            @Test
            @Order(7)
            @DisplayName("7. 기존 채팅방 없음 && 내가 상대방을 차단한 경우")
            public void startChatroomByMemberIdFailsWhenNoExistingChatroomAndBlock()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;

                Member member = blockService.blockMember(member1.getId(), member2.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member.getId(), member2.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(8)
            @DisplayName("8. 기존 채팅방 없음 && 상대방이 나를 차단한 경우")
            public void startChatroomByMemberIdFailsWhenNoExistingChatroomAndBlocked()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;

                Member member = blockService.blockMember(member2.getId(), member1.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member1.getId(), member.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(9)
            @DisplayName("9. 기존 채팅방 있음 && 상대방이 나를 차단 && 이미 퇴장한 채팅방인 경우")
            public void startChatroomByMemberIdFailsWhenBlockedAndExitedChatroomExists()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;
                // 기존 채팅방 먼저 생성
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.startChatroomByMemberId(
                    member1.getId(), member2.getId());

                // member2 -> member1 차단
                Member blockerMember = blockService.blockMember(member2.getId(), member1.getId());

                // member1 채팅방 퇴장
                chatCommandService.exitChatroom(chatroomEnterDTO.getUuid(), member1.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(member1.getId(),
                        blockerMember.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }


            @Test
            @Order(10)
            @DisplayName("10. 기존 채팅방 있음 && 내가 상대방을 차단한 경우")
            public void startChatroomByMemberIdFailsWhenExistChatroomAndBlock() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;

                // 기존 채팅방 먼저 생성
                chatCommandService.startChatroomByMemberId(member1.getId(), member2.getId());

                Member blockerMember = blockService.blockMember(member1.getId(), member2.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMemberId(blockerMember.getId(),
                        member2.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }

    @Nested
    @DisplayName("특정 글을 보고 채팅 시작")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StartChatroomByBoardId {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @Test
            @Order(11)
            @DisplayName("11. 기존 채팅방 없는 경우")
            public void startChatroomByBoardIdSucceedsWhenNoExistingChatroom() throws Exception {
                // when
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(chatroomEnterDTO);
                assertEquals(member2.getId(), chatroomEnterDTO.getMemberId());
                assertEquals(chatroomEnterDTO.getSystem().getFlag(), 1);
                assertEquals(chatroomEnterDTO.getSystem().getBoardId(), member2Board.getId());

                // 2. 데이터베이스에서 채팅방이 실제로 생성되었는지 검증
                Optional<Chatroom> createdChatroom = chatroomRepository.findByUuid(
                    chatroomEnterDTO.getUuid());
                assertTrue(createdChatroom.isPresent());

                // 3. MemberChatroom 엔티티가 각 회원에 대해 잘 생성되었는지 검증
                Optional<MemberChatroom> member1Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), createdChatroom.get().getId());
                Optional<MemberChatroom> member2Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member2.getId(), createdChatroom.get().getId());

                assertTrue(member1Chatroom.isPresent());
                assertTrue(member2Chatroom.isPresent());

            }

            @Test
            @Order(12)
            @DisplayName("12. 기존 채팅방 있음 && 이미 입장한 채팅방인 경우")
            public void startChatroomByBoardIdSucceedsWhenExistingChatroomAndEntered()
                throws Exception {
                //given
                ChatroomEnterDTO beforeChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                Chatroom beforeChatroom = chatroomRepository.findByUuid(
                    beforeChatroomEnterDTO.getUuid()).get();

                MemberChatroom beforeMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), beforeChatroom.getId()).get();

                // 해당 채팅방 입장 처리
                beforeMemberChatroom.updateLastJoinDate(LocalDateTime.now());
                beforeMemberChatroom.updateLastViewDate(LocalDateTime.now());

                // when
                ChatroomEnterDTO afterChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(afterChatroomEnterDTO);
                assertEquals(member2.getId(), afterChatroomEnterDTO.getMemberId());
                assertEquals(afterChatroomEnterDTO.getSystem().getFlag(), 2);
                assertEquals(afterChatroomEnterDTO.getSystem().getBoardId(), member2Board.getId());

                // 2. 기존에 존재하던 채팅방에 입장된 것인지 검증
                assertTrue(
                    beforeChatroomEnterDTO.getUuid().equals(afterChatroomEnterDTO.getUuid()));

                // 3. chatroomRepository에 1개의 데이터만 있는지 검증
                assertTrue(chatroomRepository.count() == 1);

            }

            @Test
            @Order(13)
            @DisplayName("13. 기존 채팅방 있는 경우")
            public void startChatroomByBoardIdSucceedsWhenExistingChatroom() throws Exception {
                //given
                ChatroomEnterDTO beforeChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // when
                ChatroomEnterDTO afterChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(afterChatroomEnterDTO);
                assertEquals(member2.getId(), afterChatroomEnterDTO.getMemberId());
                assertEquals(afterChatroomEnterDTO.getSystem().getFlag(), 1);
                assertEquals(afterChatroomEnterDTO.getSystem().getBoardId(), member2Board.getId());

                // 2. 기존에 존재하던 채팅방에 입장된 것인지 검증
                assertTrue(
                    beforeChatroomEnterDTO.getUuid().equals(afterChatroomEnterDTO.getUuid()));

                // 3. chatroomRepository에 1개의 데이터만 있는지 검증
                assertTrue(chatroomRepository.count() == 1);

            }

            @Test
            @Order(14)
            @DisplayName("14. 기존 채팅방 있음 && 상대방이 나를 차단 && 이미 입장한 채팅방인 경우")
            public void startChatroomByBoardIdSucceedsWhenExistChatroomAndBlockedAndEntered()
                throws Exception {
                // given
                // 기존 채팅방 생성
                ChatroomEnterDTO oldChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                Chatroom beforeChatroom = chatroomRepository.findByUuid(
                    oldChatroomEnterDTO.getUuid()).get();

                MemberChatroom beforeMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), beforeChatroom.getId()).get();

                // 해당 채팅방 입장 처리
                beforeMemberChatroom.updateLastJoinDate(LocalDateTime.now());
                beforeMemberChatroom.updateLastViewDate(LocalDateTime.now());

                LocalDateTime beforeLastViewDate = beforeMemberChatroom.getLastViewDate();

                // 상대방이 나를 차단
                Member blockerMember = blockService.blockMember(member2.getId(), member1.getId());

                // when
                ChatroomEnterDTO newChatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // then
                // 1. ChatroomEnterDTO의 값 검증
                assertNotNull(newChatroomEnterDTO);
                assertEquals(member2.getId(), newChatroomEnterDTO.getMemberId());
                assertEquals(newChatroomEnterDTO.getBlocked(), true);
                assertNull(newChatroomEnterDTO.getSystem());

                // 2. MemberChatroom 엔티티가 잘 업데이트 되었는지 확인
                MemberChatroom afterMemberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                        member1.getId(), beforeChatroom.getId())
                    .orElseThrow(() -> new IllegalStateException("MemberChatroom not found"));

                // 3. MemberChatroom의 lastViewDate가 업데이트 되었는지 검증
                assertTrue(beforeLastViewDate.isBefore(afterMemberChatroom.getLastViewDate()));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(15)
            @DisplayName("15. 게시글을 찾을 수 없는 경우")
            public void startChatroomByBoardIdFailsWhenBoardNotFound() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BOARD_NOT_FOUND;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(), 100L);
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(16)
            @DisplayName("16. 게시글 작성자가 본인인 경우")
            public void startChatroomByBoardIdFailsWhenBoardAuthorIsSelf() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member2.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(17)
            @DisplayName("17. 게시글 작성자가 탈퇴한 경우")
            public void startChatroomByBoardIdFailsWhenBoardAuthorIsBlind() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.USER_DEACTIVATED;
                member2.deactiveMember();

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(18)
            @DisplayName("18. 기존 채팅방 없음 && 내가 상대방을 차단한 경우")
            public void startChatroomByBoardIdFailsWhenNoExistingChatroomAndBlock()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;
                blockService.blockMember(member1.getId(), member2.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(19)
            @DisplayName("19. 기존 채팅방 없음 && 상대방이 나를 차단한 경우")
            public void startChatroomByBoardIdFailsWhenNoExistingChatroomAndBlocked()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;
                blockService.blockMember(member2.getId(), member1.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(20)
            @DisplayName("20. 기존 채팅방 있음 && 상대방이 나를 차단 && 이미 퇴장한 채팅방인 경우")
            public void startChatroomByBoardIdFailsWhenBlockedAndExitedChatroomExists()
                throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;
                // 기존 채팅방 먼저 생성
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.startChatroomByBoardId(
                    member1.getId(), member2Board.getId());

                // member2 -> member1 차단
                Member blockerMember = blockService.blockMember(member2.getId(), member1.getId());

                // member1 채팅방 퇴장
                chatCommandService.exitChatroom(chatroomEnterDTO.getUuid(), member1.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(21)
            @DisplayName("21. 기존 채팅방 있음 && 내가 상대방을 차단한 경우")
            public void startChatroomByBoardIdFailsWhenExistingChatroomAndBlock() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;
                chatCommandService.startChatroomByBoardId(member1.getId(), member2Board.getId());
                blockService.blockMember(member1.getId(), member2.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByBoardId(member1.getId(),
                        member2Board.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }


    }

    @Nested
    @DisplayName("매칭을 통한 채팅 시작")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StartChatroomByMatching {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(22)
            @DisplayName("22. 기존 채팅방 없는 경우")
            public void startChatroomByMatchingSucceedsNoExistingChatroom() throws Exception {
                // given

                // when
                String uuid = chatCommandService.startChatroomByMatching(member1.getId(),
                    member2.getId());

                // then
                // 1. 데이터베이스에서 채팅방이 실제로 생성되었는지 검증
                Optional<Chatroom> createdChatroom = chatroomRepository.findByUuid(uuid);
                assertTrue(createdChatroom.isPresent());

                // 2. MemberChatroom 엔티티가 각 회원에 대해 잘 생성되었는지 검증
                Optional<MemberChatroom> member1Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), createdChatroom.get().getId());
                Optional<MemberChatroom> member2Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member2.getId(), createdChatroom.get().getId());

                assertTrue(member1Chatroom.isPresent());
                assertTrue(member2Chatroom.isPresent());

                // 3. 각 MemberChatroom의 lastJoinDate 검증
                assertNotNull(member1Chatroom.get().getLastJoinDate());
                assertNotNull(member2Chatroom.get().getLastJoinDate());

                // 4. systemChat 전송 검증
                assertEquals(chatRepository.count(), 2);

                List<Chat> chats = chatRepository.findAll();
                assertEquals(chats.get(0).getFromMember(), systemMember);
                assertEquals(chats.get(1).getFromMember(), systemMember);

                assertEquals(chats.get(0).getToMember(), member1);
                assertEquals(chats.get(1).getToMember(), member2);

                assertEquals(chats.get(0).getContents(), MATCHING_SYSTEM_MESSAGE);
                assertEquals(chats.get(1).getContents(), MATCHING_SYSTEM_MESSAGE);
            }

            @Test
            @Order(23)
            @DisplayName("23. 기존 채팅방 있음 && 내가 입장한 채팅방인 경우")
            public void startChatroomByMatchingSucceedsWhenExistingChatroomAndEntered()
                throws Exception {
                // given
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(LocalDateTime.now())
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom1);

                // when
                String uuid = chatCommandService.startChatroomByMatching(member1.getId(),
                    member2.getId());

                // then
                // 1. member2의 MemberChatroom의 lastJoinDate 검증
                Optional<MemberChatroom> member2Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member2.getId(), savedChatroom.getId());
                assertNotNull(member2Chatroom.get().getLastJoinDate());

                // 2. systemChat 전송 검증
                assertEquals(chatRepository.count(), 2);

                List<Chat> chats = chatRepository.findAll();
                assertEquals(chats.get(0).getFromMember(), systemMember);
                assertEquals(chats.get(1).getFromMember(), systemMember);

                assertEquals(chats.get(0).getToMember(), member1);
                assertEquals(chats.get(1).getToMember(), member2);

                assertEquals(chats.get(0).getContents(), MATCHING_SYSTEM_MESSAGE);
                assertEquals(chats.get(1).getContents(), MATCHING_SYSTEM_MESSAGE);
            }

            @Test
            @Order(24)
            @DisplayName("24. 기존 채팅방 있는 경우")
            public void startChatroomByMatchingSucceedsWhenExistingChatroom() throws Exception {
                // given
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom1);

                // when
                String uuid = chatCommandService.startChatroomByMatching(member1.getId(),
                    member2.getId());

                // then
                // 1. member1의 MemberChatroom의 lastJoinDate 검증
                Optional<MemberChatroom> member1Chatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), savedChatroom.getId());
                assertNotNull(member1Chatroom.get().getLastJoinDate());

                // 2. systemChat 전송 검증
                assertEquals(chatRepository.count(), 2);

                List<Chat> chats = chatRepository.findAll();
                assertEquals(chats.get(0).getFromMember(), systemMember);
                assertEquals(chats.get(1).getFromMember(), systemMember);

                assertEquals(chats.get(0).getToMember(), member1);
                assertEquals(chats.get(1).getToMember(), member2);

                assertEquals(chats.get(0).getContents(), MATCHING_SYSTEM_MESSAGE);
                assertEquals(chats.get(1).getContents(), MATCHING_SYSTEM_MESSAGE);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(25)
            @DisplayName("25. 매칭 대상 회원으로 동일한 회원을 요청한 경우")
            public void startChatroomByMatchingFailsWhenMemberIdsAreSame() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_MEMBER_ID_INVALID;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMatching(member1.getId(), member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());
            }

            @Test
            @Order(26)
            @DisplayName("26. 매칭 대상 회원이 탈퇴한 경우")
            public void startChatroomByMatchingFailsWhenTargetMemberIsBlind() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.USER_DEACTIVATED;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMatching(member1.getId(),
                        blindMember.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());
            }

            @Test
            @Order(27)
            @DisplayName("27. 매칭 대상 회원에게 차단 당한 경우")
            public void startChatroomByMatchingFailsWhenBlocked() throws Exception {
                // given
                // 상대방이 나를 차단
                Member blockerMember = blockService.blockMember(member2.getId(), member1.getId());

                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMatching(member1.getId(), member2.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(28)
            @DisplayName("28. 매칭 대상 회원을 차단한 경우")
            public void startChatroomByMatchingFailsWhenBlock() throws Exception {
                // given
                // 내가 상대방을 차단
                Member blockerMember = blockService.blockMember(member1.getId(), member2.getId());

                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.startChatroomByMatching(member1.getId(), member2.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }

    @Nested
    @DisplayName("uuid를 통한 채팅방 입장")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EnterChatroom {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(29)
            @DisplayName("29. 기존 채팅방 있는 경우")
            public void enterChatroomSucceeds() throws Exception {
                // given
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom1);

                // when
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.enterChatroom(
                    savedChatroom.getUuid(), member1.getId());

                // then
                // 1. 입장한 채팅방 uuid 검증
                assertEquals(chatroomEnterDTO.getUuid(), savedChatroom.getUuid());

                // 2. lastViewDate update 검증
                MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), savedChatroom.getId()).get();
                assertNotNull(memberChatroom.getLastViewDate());

                // 3. 응답 dto 상대 회원 검증
                assertEquals(chatroomEnterDTO.getMemberId(), member2.getId());
            }

            @Test
            @Order(30)
            @DisplayName("30. 기존 채팅방 있음 && 상대가 나를 차단 && 이미 입장한 채팅방인 경우")
            public void enterChatroomSucceedsWhenBlockedAndEntered() throws Exception {
                // given
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom1);

                // 상대가 나를 차단
                blockService.blockMember(member2.getId(), member1.getId());

                // when
                ChatroomEnterDTO chatroomEnterDTO = chatCommandService.enterChatroom(
                    savedChatroom.getUuid(), member1.getId());

                // then
                // 1. 입장한 채팅방 uuid 검증
                assertEquals(chatroomEnterDTO.getUuid(), savedChatroom.getUuid());

                // 2. lastViewDate update 검증
                MemberChatroom memberChatroom = memberChatroomRepository.findByMemberIdAndChatroomId(
                    member1.getId(), savedChatroom.getId()).get();
                assertNotNull(memberChatroom.getLastViewDate());

                // 3. 응답 dto 상대 회원 검증
                assertEquals(chatroomEnterDTO.getMemberId(), member2.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(31)
            @DisplayName("31. uuid에 해당하는 채팅방이 없는 경우")
            public void enterChatroomFailedWhenNoExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_NOT_EXIST;

                String newUuid = UUID.randomUUID().toString();

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.enterChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(32)
            @DisplayName("32. 해당 채팅방이 회원의 것이 아닌 경우")
            public void enterChatroomFailedWhenNotOwner() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_ACCESS_DENIED;

                // member2, member3 사이 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                MemberChatroom memberChatroom3 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom3.setMember(member3);
                memberChatroomRepository.save(memberChatroom3);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.enterChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(33)
            @DisplayName("33. 상대에게 차단 당함 && 이미 퇴장한 채팅방인 경우")
            public void enterChatroomFailedWhenBlockedAndExit() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_CHAT_START_FAILED;

                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // 상대가 나를 차단
                blockService.blockMember(member2.getId(), member1.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.enterChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(34)
            @DisplayName("34. 상대를 차단한 경우")
            public void enterChatroomFailedWhenBlock() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_CHAT_START_FAILED;

                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                Chatroom savedChatroom = chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // 내가 상대를 차단
                blockService.blockMember(member1.getId(), member2.getId());

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.enterChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }

    @Nested
    @DisplayName("채팅 등록")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AddChat {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(35)
            @DisplayName("35. 시스템 메시지 없음 && 기존에 입장한 채팅방인 경우")
            public void enterChatroomSucceedsWhenNoSystemAndEntered() throws Exception {
                // given
                // 채팅방 생성 및 member1 입장
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom1 = memberChatroomRepository.save(
                    memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                MemberChatroom savedMemberChatroom2 = memberChatroomRepository.save(
                    memberChatroom2);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                Chat chat = chatCommandService.addChat(request, newUuid, member1.getId());

                // then
                // 1. 생성된 chat의 메시지 내용, member 검증
                assertEquals(chat.getContents(), newMessage);
                assertEquals(chat.getFromMember(), member1);

                // 2. member1의 lastViewDate update 검증
                assertNotNull(savedMemberChatroom1.getLastViewDate());

                // 3. member2의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom2.getLastJoinDate());

            }

            @Test
            @Order(36)
            @DisplayName("36. 시스템 메시지 없음 && 기존에 입장하지 않은 채팅방인 경우")
            public void enterChatroomSucceedsWhenNoSystemAndNotEntered() throws Exception {
                // given
                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom1 = memberChatroomRepository.save(
                    memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                MemberChatroom savedMemberChatroom2 = memberChatroomRepository.save(
                    memberChatroom2);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                Chat chat = chatCommandService.addChat(request, newUuid, member1.getId());

                // then
                // 1. 생성된 chat의 메시지 내용, member 검증
                assertEquals(chat.getContents(), newMessage);
                assertEquals(chat.getFromMember(), member1);

                // 2. member1의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom1.getLastJoinDate());

                // 3. member1의 lastViewDate update 검증
                assertNotNull(savedMemberChatroom1.getLastViewDate());

                // 4. member2의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom2.getLastJoinDate());

            }

            @Test
            @Order(37)
            @DisplayName("37. init 시스템 메시지 있음 && 기존에 입장하지 않은 채팅방인 경우")
            public void enterChatroomSucceedsWhenInitSystemAndNotEntered() throws Exception {
                // given
                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom1 = memberChatroomRepository.save(
                    memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                MemberChatroom savedMemberChatroom2 = memberChatroomRepository.save(
                    memberChatroom2);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";

                ChatRequest.SystemFlagRequest systemFlagRequest = new SystemFlagRequest();
                ReflectionTestUtils.setField(systemFlagRequest, "flag", 1);
                ReflectionTestUtils.setField(systemFlagRequest, "boardId", 1L);

                ReflectionTestUtils.setField(request, "message", newMessage);
                ReflectionTestUtils.setField(request, "system", systemFlagRequest);

                // when
                Chat chat = chatCommandService.addChat(request, newUuid, member1.getId());

                // then
                // 1. 생성된 chat의 메시지 내용, member 검증
                assertEquals(chat.getContents(), newMessage);
                assertEquals(chat.getFromMember(), member1);

                // 2. member1의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom1.getLastJoinDate());

                // 3. member1의 lastViewDate update 검증
                assertNotNull(savedMemberChatroom1.getLastViewDate());

                // 4. member2의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom2.getLastJoinDate());

                // 5. systemChat 생성되었는지 검증
                assertEquals(chatRepository.count(), 3);

                List<Chat> chats = chatRepository.findAll();
                assertEquals(chats.get(0).getFromMember(), systemMember);
                assertEquals(chats.get(1).getFromMember(), systemMember);

                assertEquals(chats.get(0).getToMember(), member1);
                assertEquals(chats.get(1).getToMember(), member2);

                assertEquals(chats.get(0).getContents(), POST_SYSTEM_MESSAGE_TO_MEMBER_INIT);
                assertEquals(chats.get(1).getContents(), POST_SYSTEM_MESSAGE_TO_TARGET_MEMBER);

            }

            @Test
            @Order(38)
            @DisplayName("38. 시스템 메시지 있음 && 기존에 입장한 채팅방인 경우")
            public void enterChatroomSucceedsWhenSystemAndEntered() throws Exception {
                // given
                // 채팅방 생성 및 member1 입장
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom1 = memberChatroomRepository.save(
                    memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                MemberChatroom savedMemberChatroom2 = memberChatroomRepository.save(
                    memberChatroom2);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";

                ChatRequest.SystemFlagRequest systemFlagRequest = new SystemFlagRequest();
                ReflectionTestUtils.setField(systemFlagRequest, "flag", 2);
                ReflectionTestUtils.setField(systemFlagRequest, "boardId", 1L);

                ReflectionTestUtils.setField(request, "message", newMessage);
                ReflectionTestUtils.setField(request, "system", systemFlagRequest);

                // when
                Chat chat = chatCommandService.addChat(request, newUuid, member1.getId());

                // then
                // 1. 생성된 chat의 메시지 내용, member 검증
                assertEquals(chat.getContents(), newMessage);
                assertEquals(chat.getFromMember(), member1);

                // 2. member1의 lastViewDate update 검증
                assertNotNull(savedMemberChatroom1.getLastViewDate());

                // 3. member2의 lastJoinDate update 검증
                assertNotNull(savedMemberChatroom2.getLastJoinDate());

                // 4. systemChat 생성되었는지 검증
                assertEquals(chatRepository.count(), 3);

                List<Chat> chats = chatRepository.findAll();
                assertEquals(chats.get(0).getFromMember(), systemMember);
                assertEquals(chats.get(1).getFromMember(), systemMember);

                assertEquals(chats.get(0).getToMember(), member1);
                assertEquals(chats.get(1).getToMember(), member2);

                assertEquals(chats.get(0).getContents(), POST_SYSTEM_MESSAGE_TO_MEMBER);
                assertEquals(chats.get(1).getContents(), POST_SYSTEM_MESSAGE_TO_TARGET_MEMBER);

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(39)
            @DisplayName("39. uuid에 해당하는 채팅방이 없는 경우")
            public void enterChatroomFailedWhenNoExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_NOT_EXIST;

                String newUuid = UUID.randomUUID().toString();

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.addChat(request, newUuid, member1.getId());

                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(40)
            @DisplayName("40. 해당 채팅방이 회원의 것이 아닌 경우")
            public void enterChatroomFailedWhenNotOwner() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_ACCESS_DENIED;

                // member2, member3 사이 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                MemberChatroom memberChatroom3 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom3.setMember(member3);
                memberChatroomRepository.save(memberChatroom3);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.addChat(request, newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(41)
            @DisplayName("41. 내가 상대에게 차단 당한 경우")
            public void enterChatroomFailedWhenBlocked() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.BLOCKED_BY_CHAT_TARGET_SEND_CHAT_FAILED;

                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // 상대가 나를 차단
                blockService.blockMember(member2.getId(), member1.getId());

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.addChat(request, newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(42)
            @DisplayName("42. 내가 상대를 차단한 경우")
            public void enterChatroomFailedWhenBlock() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_TARGET_IS_BLOCKED_SEND_CHAT_FAILED;

                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // 내가 상대를 차단
                blockService.blockMember(member1.getId(), member2.getId());

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.addChat(request, newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(43)
            @DisplayName("43. 상대가 탈퇴한 회원인 경우")
            public void enterChatroomFailedWhenBlind() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.USER_DEACTIVATED;

                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(blindMember);
                memberChatroomRepository.save(memberChatroom2);

                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.addChat(request, newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }

    @Nested
    @DisplayName("채팅 메시지 읽음 처리")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReadChatMessages {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(44)
            @DisplayName("44. timestamp가 null인 경우")
            public void readChatMessageSucceedsWhenTimestampIsNull() throws Exception {
                // given
                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom = memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(blindMember);
                memberChatroomRepository.save(memberChatroom2);

                // when
                chatCommandService.readChatMessages(newUuid, null, member1.getId());

                // then
                assertNotNull(savedMemberChatroom.getLastViewDate());
            }

            @Test
            @Order(45)
            @DisplayName("45. timestamp에 해당하는 채팅 메시지가 존재하는 경우")
            public void readChatMessageSucceedsWhenTimestampMsgExists() throws Exception {
                // given
                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                MemberChatroom savedMemberChatroom = memberChatroomRepository.save(memberChatroom2);

                // 채팅 메시지 등록
                // 채팅 메시지 dto 생성
                ChatRequest.ChatCreateRequest request = new ChatCreateRequest();
                String newMessage = "test message";
                ReflectionTestUtils.setField(request, "message", newMessage);

                Chat chat = chatCommandService.addChat(request, newUuid, member1.getId());

                // when
                chatCommandService.readChatMessages(newUuid, chat.getTimestamp(), member2.getId());

                // then
                assertEquals(savedMemberChatroom.getLastViewDate(), chat.getCreatedAt());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(46)
            @DisplayName("46. uuid에 해당하는 채팅방이 없는 경우")
            public void readChatMessageFailedWhenNoExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_NOT_EXIST;

                String newUuid = UUID.randomUUID().toString();

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.readChatMessages(newUuid, null, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(47)
            @DisplayName("47. 해당 채팅방이 회원의 것이 아닌 경우")
            public void readChatMessageFailedWhenNotOwner() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_ACCESS_DENIED;

                // member2, member3 사이 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                MemberChatroom memberChatroom3 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom3.setMember(member3);
                memberChatroomRepository.save(memberChatroom3);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.readChatMessages(newUuid, null, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(48)
            @DisplayName("48. timestamp에 해당하는 채팅 메시지가 존재하지 않는 경우")
            public void readChatMessageFailedWhenTimestampMsgNotExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHAT_MESSAGE_NOT_FOUND;

                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.readChatMessages(newUuid, 1000000000000L, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }

    @Nested
    @DisplayName("채팅방 나가기")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ExitChatroom {

        @Nested
        @DisplayName("성공 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SuccessCase {

            @Test
            @Order(49)
            @DisplayName("49. 채팅방 퇴장 성공")
            public void exitChatroomSucceed() throws Exception {
                // given
                // 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom1 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom1.setMember(member1);
                MemberChatroom savedMemberChatroom = memberChatroomRepository.save(memberChatroom1);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                // when
                chatCommandService.exitChatroom(newUuid, member1.getId());

                // then
                assertNull(savedMemberChatroom.getLastJoinDate());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class FailCase {

            @Test
            @Order(50)
            @DisplayName("50. uuid에 해당하는 채팅방이 없는 경우")
            public void exitChatroomFailedWhenNoExists() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_NOT_EXIST;

                String newUuid = UUID.randomUUID().toString();

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.exitChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }

            @Test
            @Order(51)
            @DisplayName("51. 해당 채팅방이 회원의 것이 아닌 경우")
            public void exitChatroomFailedWhenNotOwner() throws Exception {
                // given
                ErrorStatus expectedErrorCode = ErrorStatus.CHATROOM_ACCESS_DENIED;

                // member2, member3 사이 채팅방 생성
                String newUuid = UUID.randomUUID().toString();
                Chatroom newChatroom = Chatroom.builder()
                    .uuid(newUuid)
                    .startMember(null)
                    .build();

                chatroomRepository.save(newChatroom);

                MemberChatroom memberChatroom2 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(null)
                    .chatroom(newChatroom)
                    .build();
                memberChatroom2.setMember(member2);
                memberChatroomRepository.save(memberChatroom2);

                MemberChatroom memberChatroom3 = MemberChatroom.builder()
                    .lastViewDate(null)
                    .lastJoinDate(LocalDateTime.now())
                    .chatroom(newChatroom)
                    .build();
                memberChatroom3.setMember(member3);
                memberChatroomRepository.save(memberChatroom3);

                // when
                GeneralException exception = assertThrows(GeneralException.class, () -> {
                    chatCommandService.exitChatroom(newUuid, member1.getId());
                });

                // then
                assertEquals(expectedErrorCode, exception.getCode());

            }
        }
    }
}
