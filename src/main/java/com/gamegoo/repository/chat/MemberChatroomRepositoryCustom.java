package com.gamegoo.repository.chat;

import com.gamegoo.domain.chat.MemberChatroom;
import java.util.List;

public interface MemberChatroomRepositoryCustom {

    List<MemberChatroom> findActiveMemberChatroomOrderByLastChat(Long memberId);


}
