package com.gamegoo.repository.friend;

import com.gamegoo.domain.friend.Friend;
import org.springframework.data.domain.Slice;

interface FriendRepositoryCustom {

    Slice<Friend> findFriendsByCursorAndOrdered(Long cursor, Long memberId, Integer pageSize);
}
