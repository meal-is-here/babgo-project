package com.babgo.controller.store;

import com.babgo.repository.store.StoreSseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class StoreSseController {

    private final StoreSseHub sseHub;

    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // TODO : @AuthenticationPrincipal 사용해서 userId값
        Long userId = 1L;
        return sseHub.subscribe(userId);
    }
}
