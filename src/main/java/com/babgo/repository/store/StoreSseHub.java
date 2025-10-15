package com.babgo.repository.store;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StoreSseHub {

    private static final long DEFAULT_TIMEOUT = 60L * 60 * 1000;
    private final Map<Long, SseEmitter> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emittersByUser.put(userId, emitter);

        emitter.onTimeout(() -> emittersByUser.remove(userId));
        emitter.onCompletion(() -> emittersByUser.remove(userId));
        emitter.onError(e -> emittersByUser.remove(userId));

        return emitter;
    }

    public void notify(Long userId, String eventName, Object payload) {
        SseEmitter emitter = emittersByUser.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .id(Instant.now().toString())
                    .data(payload, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            // 끊긴 연결 정리
            emittersByUser.remove(userId);
            emitter.completeWithError(e);
        }
    }
}
