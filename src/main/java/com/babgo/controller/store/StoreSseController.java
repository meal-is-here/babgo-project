package com.babgo.controller.store;

import com.babgo.repository.store.StoreSseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class StoreSseController {

    private final StoreSseHub sseHub;

    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("userId") Long userId) {
        // TODO : @AuthenticationPrincipal 사용해서 userId값
        return sseHub.subscribe(userId);
    }
}
