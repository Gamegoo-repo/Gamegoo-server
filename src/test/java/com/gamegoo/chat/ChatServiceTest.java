package com.gamegoo.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamegoo.domain.chat.Chatroom;
import com.gamegoo.domain.chat.MemberChatroom;
import com.gamegoo.domain.member.LoginType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.dto.chat.ChatResponse.ChatroomEnterDTO;
import com.gamegoo.repository.chat.ChatroomRepository;
import com.gamegoo.repository.chat.MemberChatroomRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.chat.ChatCommandService;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ChatServiceTest {

    @Autowired
    private ChatCommandService chatCommandService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private MemberChatroomRepository memberChatroomRepository;

    private Member member1;

    private Member member2;

    @BeforeEach
    public void setUp() {
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
            .build();

        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);
    }

    @Test
    @DisplayName("특정 회원과 채팅방 시작, 기존에 채팅방 없는 경우 - 성공")
    public void successStartChatroomByMemberIdAndCreateNewChatroom() throws Exception {
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


}
