package com.study.odersystem.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    // ConcurrentHashMap은 Tread-safe한 map(동시성 이슈 발생 X)
    Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void registerEmitter(String email, SseEmitter emitter) {
        emitterMap.put(email, emitter);
    }

    public void removeEmitter(String email) {
        emitterMap.remove(email);
    }

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }
}
