package com.babgo.controller.store;

import com.babgo.global.security.annotation.CurrentUser;
import com.babgo.repository.store.StoreSseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class StoreSseController {

    private final StoreSseHub sseHub;

    @GetMapping(value = "/v1/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @CurrentUser Long userId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    )
    {
        return sseHub.subscribe(userId, lastEventId);
    }
}
