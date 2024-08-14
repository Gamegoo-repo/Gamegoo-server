package com.gamegoo.domain.notification;

public enum NotificationTypeTitle {
    FRIEND_REQUEST_SEND("님에게 친구 요청을 보냈어요.", null),
    FRIEND_REQUEST_RECEIVED("님에게 친구 요청이 왔어요.", "/member/profile/"),
    FRIEND_REQUEST_ACCEPTED("님이 친구를 수락했어요.", null),
    FRIEND_REQUEST_REJECTED("님이 친구를 거절했어요.", null),
    MANNER_LEVEL_UP("매너레벨이 n단계로 올라갔어요!", "/member/manner"),
    MANNER_LEVEL_DOWN("매너레벨이 n단계로 떨어졌어요.", "/member/manner"),
    MANNER_KEYWORD_RATED("지난 매칭에서 n 키워드를 받았어요. 자세한 내용은 내정보에서 확인하세요!", "/member/manner"),
    TEST_ALARM("TEST PUSH. NUMBER: ", null),
    ;

    private final String content;
    private final String sourceUrl;

    NotificationTypeTitle(String content, String sourceUrl) {
        this.content = content;
        this.sourceUrl = sourceUrl;
    }

    public String getMessage() {
        return content;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }
}
