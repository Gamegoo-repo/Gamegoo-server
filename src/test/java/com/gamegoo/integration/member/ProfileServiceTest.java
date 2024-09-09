package com.gamegoo.integration.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.GeneralException;
import com.gamegoo.domain.member.LoginType;
import com.gamegoo.domain.member.Member;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.member.ProfileService;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private MemberRepository memberRepository;

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
    @DisplayName("memberId로 member 찾기 - 성공")
    public void successFindMemberById() throws Exception {
        // when
        System.out.println("member1 = " + member1.getId());
        Member testMember = profileService.findMember(member1.getId());  // 실제 서비스 호출

        // then
        assertNotNull(member1);  // 결과가 null이 아님을 확인
        assertEquals(member1.getId(), testMember.getId());  // ID 비교
        assertEquals(member1.getEmail(), testMember.getEmail());  // 이메일 비교
    }

    @Test
    @DisplayName("memberId로 member 찾기 - 실패")
    public void failFindMemberById() throws Exception {
        // given
        Long nonExistentMemberId = -1L;
        ErrorStatus expectedErrorCode = ErrorStatus.MEMBER_NOT_FOUND;  // 기대하는 에러 코드

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            profileService.findMember(nonExistentMemberId);
        });

        // 발생한 예외의 code가 기대하는 값인지 확인
        assertEquals(expectedErrorCode, exception.getCode());
    }

}
