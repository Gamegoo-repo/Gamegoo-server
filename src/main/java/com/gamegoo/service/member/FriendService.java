package com.gamegoo.service.member;

import com.gamegoo.domain.Friend;
import com.gamegoo.repository.member.FriendRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendRepository friendRepository;

    /**
     * memberId에 해당하는 회원의 친구 목록 조회
     *
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public List<Friend> getFriends(Long memberId) {
        return friendRepository.findAllByFromMemberId(memberId);
    }


}
