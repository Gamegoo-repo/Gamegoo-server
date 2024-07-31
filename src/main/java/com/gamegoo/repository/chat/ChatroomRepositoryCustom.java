package com.gamegoo.repository.chat;

import com.gamegoo.domain.chat.Chatroom;
import java.util.Optional;

public interface ChatroomRepositoryCustom {

    Optional<Chatroom> findChatroomByMemberIds(Long memberId1, Long memberId2);

}
