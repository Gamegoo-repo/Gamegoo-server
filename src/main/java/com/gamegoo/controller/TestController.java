package com.gamegoo.controller;

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
}
