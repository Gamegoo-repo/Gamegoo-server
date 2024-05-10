package com.gamegoo.controller;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class TestController {

    @GetMapping("/test/hello")
    @Operation(summary = "swagger 테스트용 API 입니다.", description = "simple API for swagger test!")
    public String hello() {
        return "Swagger Setting Success!";
    }

    @GetMapping("/test/error")
    @Operation(summary = "에러 통일 테스트용 API 입니다.", description = "simple API for API Error Response!")
    public String apiResponseTest() {
        throw new TempHandler(ErrorStatus.TEMP_EXCEPTION);
    }
}
