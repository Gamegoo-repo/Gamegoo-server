package com.gamegoo.repository.chat;

import com.gamegoo.domain.chat.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatRepositoryCustom {

    Integer countUnreadChats(Long chatroomId, Long memberChatroomId);

    Slice<Chat> findRecentChats(Long chatroomId, Long memberChatroomId);

    Slice<Chat> findChatsByCursor(Long cursor, Long chatroomId, Pageable pageable);


}
