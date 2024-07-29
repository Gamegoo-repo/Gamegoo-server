package com.gamegoo.repository.chat;

import com.gamegoo.domain.chat.Chat;
import com.gamegoo.domain.chat.Chatroom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long>, ChatRepositoryCustom {

    Optional<Chat> findFirstByChatroomIdOrderByCreatedAtDesc(Long chatroomId);

    Optional<Chat> findByChatroomAndTimestamp(Chatroom chatroom, Long timestamp);

}
