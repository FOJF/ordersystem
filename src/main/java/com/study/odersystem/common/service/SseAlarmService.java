package com.study.odersystem.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.common.dto.SseOrderingMessageDto;
import com.study.odersystem.common.repository.SseEmitterRegistry;
import com.study.odersystem.ordering.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate<String, String> ssePubSubTemplate;
    private final RedisTemplate redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("ssePubSub") RedisTemplate<String, String> ssePubSubTemplate, RedisTemplate redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.ssePubSubTemplate = ssePubSubTemplate;
        this.redisTemplate = redisTemplate;
    }

    // 특정 사용자에게 메세지 발송
    public void publishOrderingMessage(String receiver, String sender, Long orderingId, OrderStatus orderStatus) {
        SseOrderingMessageDto sseOrderingMessageDto = SseOrderingMessageDto.builder()
                .sender(sender)
                .receiver(receiver)
                .orderingId(orderingId)
                .orderStatus(orderStatus)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;

        try {
            data = objectMapper.writeValueAsString(sseOrderingMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // emitter 객체를 통해 사용자 정보를 전송
        SseEmitter emitter = this.sseEmitterRegistry.getEmitter(receiver);

        // 다중서버인 경우, 현재 서버의 메모리 없으면, redis의 pub/sub기능을 이용해 다른 서버에 던짐
        if (emitter == null) {
            redisTemplate.convertAndSend("ordering", data);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name("ordering").data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(14400 * 60 * 1000L); // 10일 정도 emitter 유효기간
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        this.sseEmitterRegistry.registerEmitter(email, emitter);

        try {
            emitter.send(SseEmitter.event().name("connected").data("연결 완료"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return emitter;
    }

    public void disconnect() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        this.sseEmitterRegistry.removeEmitter(email);
    }

    //
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // message : 실질적인 메세지가 담겨있는 객체
        // pattern : 채널명
        ObjectMapper objectMapper = new ObjectMapper();
        SseOrderingMessageDto dto = null;
        try {
            dto = objectMapper.readValue(message.getBody(), SseOrderingMessageDto.class);
            System.out.println("dto : " + dto);
            System.out.println("channel : " + new String(pattern));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SseEmitter emitter = this.sseEmitterRegistry.getEmitter(dto.getReceiver());

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("ordering").data(message.getBody()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
