package com.gamegoo.service.chat;

import com.gamegoo.repository.chat.ChatroomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final ChatroomRepository chatroomRepository;


    /**
     * 해당 회원의 ACTIVE한 채팅방의 uuid list를 리턴
     *
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public List<String> getChatroomUuids(Long memberId) {
        return chatroomRepository.findActiveChatroomUuidsByMemberId(memberId);
    }

}
