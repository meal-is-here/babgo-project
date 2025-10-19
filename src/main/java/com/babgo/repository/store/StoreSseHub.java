package com.babgo.repository.store;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StoreSseHub {

    private static final long DEFAULT_TIMEOUT = 60L * 60 * 1000;        // 1시간 연결 유지
    private static final long CLIENT_RETRY_MS = 3000;
    private static final int  MAX_EVENT_BUFFER = 100;                   // 유저별로 최근 100개 이벤트만 보관

    private final Map<Long, SseEmitter> emittersByUser = new ConcurrentHashMap<>();
    private final Map<Long, Deque<ServerEvent>> eventBufferByUser = new ConcurrentHashMap<>();


    public SseEmitter subscribe(Long userId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        SseEmitter prev = emittersByUser.put(userId, emitter);
        if (prev != null) {
            try { prev.complete(); } catch (Exception ignore) {}
        }

        emitter.onTimeout(() -> emittersByUser.remove(userId));
        emitter.onCompletion(() -> emittersByUser.remove(userId));
        emitter.onError(e -> emittersByUser.remove(userId));

        try {
            String initId = nowId();
            Map<String, Object> initPayload = Map.of(
                    "type", "INIT",
                    "at", Instant.now().toString(),
                    "lastEventId", initId
            );
            emitter.send(SseEmitter.event()
                    .reconnectTime(CLIENT_RETRY_MS)
                    .name("INIT")
                    .id(initId)
                    .data(initPayload, MediaType.APPLICATION_JSON));

            // 재연결이라면: 클라가 보낸 Last-Event-ID 이후의 이벤트만 재전송(유실 방지)
            if (lastEventId != null && !lastEventId.isBlank()) {
                Deque<ServerEvent> buf = eventBufferByUser.get(userId);
                if (buf != null && !buf.isEmpty()) {
                    ServerEvent[] snapshot;
                    synchronized (buf) {
                        snapshot = buf.toArray(new ServerEvent[0]);
                    }
                    for (ServerEvent e : snapshot) {
                        if (compareEventId(e.id(), lastEventId) > 0) {  // lastEventId 이후만
                            emitter.send(SseEmitter.event()
                                    .reconnectTime(CLIENT_RETRY_MS)
                                    .name(e.name())
                                    .id(e.id())
                                    .data(e.payload(), MediaType.APPLICATION_JSON));
                        }
                    }
                }
            }
        } catch (IOException e) {
            emittersByUser.remove(userId);
            emitter.completeWithError(e);
        }
        return emitter;
    }


    public void notify(Long userId, String eventName, Map<String, Object> payload) {
        SseEmitter emitter = emittersByUser.get(userId);
        String id = nowId();

        Map<String, Object> enriched = enrichMap(payload, id);

        Deque<ServerEvent> buf = eventBufferByUser.computeIfAbsent(userId, k -> new ArrayDeque<>(MAX_EVENT_BUFFER));
        synchronized (buf) {
            if (buf.size() >= MAX_EVENT_BUFFER) buf.pollFirst();
            buf.offerLast(new ServerEvent(id, eventName, enriched));
        }

        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .reconnectTime(CLIENT_RETRY_MS)
                    .name(eventName)
                    .id(id)
                    .data(enriched, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emittersByUser.remove(userId);
            emitter.completeWithError(e);
        }
    }

    // ----------------------- 내부 유틸 -----------------------

    private static String nowId() {
        return String.valueOf(System.currentTimeMillis());
    }

    // 이벤트 ID 비교
    private static int compareEventId(String a, String b) {
        try {
            long la = Long.parseLong(a);
            long lb = Long.parseLong(b);
            return Long.compare(la, lb);
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    private static Map<String, Object> enrichMap(Map<String, Object> base, String id) {
        LinkedHashMap<String, Object> merged = new LinkedHashMap<>(base != null ? base.size() + 2 : 2);
        if (base != null) merged.putAll(base);
        merged.put("at", Instant.now().toString());
        merged.put("lastEventId", id);
        return merged;
    }

    // 허브 내부 버퍼 아이템(전송 메타 + 데이터)
    private record ServerEvent(String id, String name, Object payload) {}
}
