package com.study.odersystem.common.controller;

import com.study.odersystem.common.service.SseAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseAlarmController {
    private final SseAlarmService sseAlarmService;

    @GetMapping("")
    public SseEmitter connect() {
        return this.sseAlarmService.connect();
    }

    @DeleteMapping("")
    public void disconnect() {
        this.sseAlarmService.disconnect();
    }

}
