package com.gamegoo.service.notification;


import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.NotificationHandler;
import com.gamegoo.apiPayload.exception.handler.PageHandler;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.notification.Notification;
import com.gamegoo.domain.notification.NotificationType;
import com.gamegoo.domain.notification.NotificationTypeTitle;
import com.gamegoo.repository.notification.NotificationRepository;
import com.gamegoo.repository.notification.NotificationTypeRepository;
import com.gamegoo.service.member.ProfileService;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;
    private final ProfileService profileService;

    private static final int PAGE_SIZE = 5;

    /**
     * 새로운 알림 생성 및 저장 메소드
     *
     * @param notificationTypeTitle
     * @param content               각 알림에 포함되어어야 할 정보 (사용자 닉네임, 매너레벨 단계, 키둬드)
     * @param sourceId              이동해야할 url의 고유 id 파라미터, FRIEND_REQUEST_RECEIVED에서만 필요
     * @param member                알림을 받을 대상 회원
     * @return
     */
    public Notification createNotification(NotificationTypeTitle notificationTypeTitle,
        String content, Long sourceId, Member member) {

        NotificationType notificationType = notificationTypeRepository.findNotificationTypeByTitle(
                notificationTypeTitle)
            .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_TYPE_NOT_FOUND));

        switch (notificationTypeTitle) {
            case FRIEND_REQUEST_SEND:
                return createFriendRequestSendNotification(notificationType, content, member);
            case FRIEND_REQUEST_RECEIVED:
                if (sourceId == null) {
                    throw new NotificationHandler(ErrorStatus.NOTIFICATION_METHOD_BAD_REQUEST);
                }
                return createFriendRequestReceivedNotification(notificationType, content, sourceId,
                    member);
            case FRIEND_REQUEST_ACCEPTED:
            case FRIEND_REQUEST_REJECTED:
                return createFriendRequestAcceptedAndRejectedNotification(notificationType, content,
                    member);
            case MANNER_LEVEL_UP:
            case MANNER_LEVEL_DOWN:
                return createMannerLevelUpDownNotification(notificationType, content, member);
            case MANNER_KEYWORD_RATED:
                return createMannerKeywordRatedNotification(notificationType, content, member);
            case TEST_ALARM:
                // 랜덤 숫자 생성
                Random random = new Random();
                int i = random.nextInt(1000) + 1;
                // 숫자를 String 형으로 변환
                String randomNumberString = Integer.toString(i);
                return createTestNotification(notificationType, randomNumberString, member);
            default:
                throw new NotificationHandler(ErrorStatus.NOTIFICATION_TYPE_NOT_FOUND);
        }
    }

    /**
     * 알림 목록 조회, 커서 기반 페이징
     *
     * @param memberId
     * @param type
     * @param cursor
     * @return
     */
    public Slice<Notification> getNotificationListByCursor(Long memberId,
        String type, Long cursor) {
        Member member = profileService.findMember(memberId);

        if (!"general".equals(type) && !"friend".equals(type)) {
            throw new NotificationHandler(ErrorStatus.INVALID_NOTIFICATION_TYPE);
        }

        return notificationRepository.findNotificationsByCursorAndType(member.getId(), type,
            cursor);
    }

    /**
     * 알림 목록 조회, 페이지 번호 기반 페이징
     *
     * @param memberId
     * @param pageIdx
     * @return
     */
    public Page<Notification> getNotificationListByPage(Long memberId,
        Integer pageIdx) {
        Member member = profileService.findMember(memberId);

        if (pageIdx < 0) {
            throw new PageHandler(ErrorStatus.PAGE_INVALID);
        }

        PageRequest pageRequest = PageRequest.of(pageIdx, PAGE_SIZE,
            Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationRepository.findNotificationsByMember(member, pageRequest);
    }

    /**
     * 특정 알림 읽음 상태로 변경
     *
     * @param memberId
     * @param notificationId
     * @return
     */
    public Notification readNotification(Long memberId, Long notificationId) {
        Member member = profileService.findMember(memberId);

        // 알림 엔티티 조회 및 검증
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

        // 해당 알림이 회원의 것이 아닌 경우
        if (!notification.getMember().equals(member)) {
            throw new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND);
        }

        notification.updateIsRead(true);

        return notification;
    }

    /**
     * 친구 요청 전송됨 알림 생성
     *
     * @param notificationType
     * @param content
     * @param member
     * @return
     */
    private Notification createFriendRequestSendNotification(
        NotificationType notificationType,
        String content, Member member) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(content + notificationType.getContent())
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

    /**
     * 친구 요청 받음 알림 생성
     *
     * @param notificationType
     * @param content
     * @param sourceId
     * @param member
     * @return
     */
    private Notification createFriendRequestReceivedNotification(
        NotificationType notificationType,
        String content, Long sourceId, Member member
    ) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(content + notificationType.getContent())
            .sourceId(sourceId)
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

    /**
     * 친구 요청 수락됨, 거절됨 알림 생성
     *
     * @param notificationType
     * @param content
     * @param member
     * @return
     */
    private Notification createFriendRequestAcceptedAndRejectedNotification(
        NotificationType notificationType,
        String content, Member member
    ) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(content + notificationType.getContent())
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

    /**
     * 매너레벨 상승, 하락 알림 생성
     *
     * @param notificationType
     * @param content
     * @param member
     * @return
     */
    private Notification createMannerLevelUpDownNotification(NotificationType notificationType,
        String content, Member member) {
        String notificationContent = notificationType.getContent().replace("n", content);
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(notificationContent)
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

    /**
     * 키워드 평가 알림 생성
     *
     * @param notificationType
     * @param content
     * @param member
     * @return
     */
    private Notification createMannerKeywordRatedNotification(NotificationType notificationType,
        String content, Member member) {
        String notificationContent = notificationType.getContent().replace("n", content);
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(notificationContent)
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

    /**
     * 테스트 알림 생성
     *
     * @param notificationType
     * @param content
     * @param member
     * @return
     */
    private Notification createTestNotification(NotificationType notificationType, String content,
        Member member) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .content(notificationType.getContent() + content)
            .isRead(false)
            .build();
        notification.setMember(member);

        return notificationRepository.save(notification);
    }

}
