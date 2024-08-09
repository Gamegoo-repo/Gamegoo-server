package com.gamegoo.controller;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
import com.gamegoo.domain.Member.Member;
import com.gamegoo.domain.notification.NotificationTypeTitle;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.service.notification.NotificationService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class TestController {

    private final NotificationService notificationService;
    private final ProfileService profileService;

    @GetMapping("/test/hello")
    @Operation(summary = "swagger 테스트용 API 입니다.", description = "simple API for swagger test!")
    public String hello() {
        return "Swagger Setting Success%%!";
    }

    @GetMapping("/test/error")
    @Operation(summary = "에러 통일 테스트용 API 입니다.", description = "simple API for API Error Response!")
    public String apiResponseTest() {
        throw new TempHandler(ErrorStatus.TEMP_EXCEPTION);
    }

    @GetMapping("/test/send/notifications/{times}")
    @Operation(summary = "테스트용 알림 생성 API 입니다.", description = "서버 테스트용 입니다!!")
    public ApiResponse<String> sendTestNotifications(
            @PathVariable(name = "times") int times
    ) {
        Long memberId = JWTUtil.getCurrentUserId();
        Member member = profileService.findMember(memberId);

        for (int i = 0; i < times; i++) {
            notificationService.createNotification(NotificationTypeTitle.TEST_ALARM, null, null,
                    member);
        }
        return ApiResponse.onSuccess("테스트 알림 생성 성공");
    }

}
