package com.gamegoo.repository.chat;

public interface ChatRepositoryCustom {

    Integer countUnreadChats(Long chatroomId, Long memberChatroomId);

}
