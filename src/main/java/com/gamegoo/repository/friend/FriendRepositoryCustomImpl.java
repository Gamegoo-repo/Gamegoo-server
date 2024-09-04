package com.gamegoo.repository.friend;

import static com.gamegoo.domain.friend.QFriend.friend;

import com.gamegoo.domain.friend.Friend;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@Slf4j
@RequiredArgsConstructor
public class FriendRepositoryCustomImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Friend> findFriendsByCursorAndOrdered(Long cursorId, Long memberId,
        Integer pageSize) {
        // 전체 친구 목록 조회
        List<Friend> allFriends = queryFactory.selectFrom(friend)
            .where(friend.fromMember.id.eq(memberId))
            .fetch();

        // 친구 목록 전체 정렬
        allFriends.sort(
            (f1, f2) -> memberNameComparator.compare(f1.getToMember().getGameName(),
                f2.getToMember().getGameName()));

        // 정렬된 데이터에서 페이징 적용
        // cursorId에 해당하는 요소의 인덱스 찾기
        int startIndex = 0;
        if (cursorId != null) {
            startIndex = findCursorIndex(allFriends, cursorId);
        }

        List<Friend> pagedFriends = allFriends.stream()
            .skip(startIndex != 0 ? startIndex + 1 : 0) // cursorId가 null이 아니면 인덱스 다음부터, null이면 처음부터
            .limit(pageSize + 1) // 다음 페이지가 있는지 확인하기 위해 +1
            .collect(Collectors.toList());

        boolean hasNext = false;
        if (pagedFriends.size() > pageSize) {
            pagedFriends.remove(pageSize.intValue());
            hasNext = true;
        }

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        return new SliceImpl<>(pagedFriends, pageRequest, hasNext);
    }

    @Override
    public List<Friend> findFriendsByQueryStringAndOrdered(String queryString, Long memberId) {
        // query string으로 시작하는 소환사명을 갖는 모든 친구 목록 조회
        List<Friend> result = queryFactory.selectFrom(friend)
            .where(friend.fromMember.id.eq(memberId)
                .and(friend.toMember.gameName.startsWith(queryString))
            )
            .fetch();

        result.sort(
            (f1, f2) -> memberNameComparator.compare(f1.getToMember().getGameName(),
                f2.getToMember().getGameName()));

        return result;
    }

    // cursorId에 해당하는 Friend 객체의 인덱스 찾기
    private int findCursorIndex(List<Friend> allFriends, Long cursorId) {
        for (int i = 0; i < allFriends.size(); i++) {
            if (allFriends.get(i).getToMember().getId().equals(cursorId)) {
                return i;
            }
        }
        return 0; // cursorId에 해당하는 객체를 찾지 못하면 처음부터 시작
    }


    private static final Comparator<String> memberNameComparator = (s1, s2) -> {
        int length1 = s1.length();
        int length2 = s2.length();
        int minLength = Math.min(length1, length2);

        // 각 문자 비교
        for (int i = 0; i < minLength; i++) {
            int result = compareChars(s1.charAt(i), s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }

        // 앞부분이 동일하면, 길이가 짧은 것이 앞으로 오도록 정렬
        return Integer.compare(length1, length2);
    };

    /**
     * 문자 비교 메서드: 한글 -> 영문자 -> 숫자 순으로 우선순위 지정
     *
     * @param c1
     * @param c2
     * @return
     */
    private static int compareChars(char c1, char c2) {
        boolean isC1Korean = isKorean(c1);
        boolean isC2Korean = isKorean(c2);

        // 한글과 영문자/숫자를 구분하여 우선순위 설정
        if (isC1Korean && !isC2Korean) {
            return -1; // 한글은 영문자/숫자보다 먼저
        } else if (!isC1Korean && isC2Korean) {
            return 1; // 영문자/숫자는 한글보다 뒤
        } else if (Character.isDigit(c1) && Character.isDigit(c2)) {
            return Character.compare(c1, c2); // 둘 다 숫자인 경우 숫자 비교
        } else if (Character.isDigit(c1)) {
            return 1; // 숫자는 항상 뒤로
        } else if (Character.isDigit(c2)) {
            return -1; // 숫자는 항상 뒤로
        } else {
            return Character.compare(c1, c2); // 기본적으로 문자 비교 (영문자끼리 등)
        }
    }

    /**
     * 한글 여부를 판별
     *
     * @param c
     * @return
     */
    private static boolean isKorean(char c) {
        return (c >= 0x1100 && c <= 0x11FF) || // 한글 자모
            (c >= 0xAC00 && c <= 0xD7AF) || // 한글 음절
            (c >= 0x3130 && c <= 0x318F);   // 한글 호환 자모
    }
}
