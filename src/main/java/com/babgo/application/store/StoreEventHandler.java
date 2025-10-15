package com.babgo.application.store;

import com.babgo.domain.store.StoreEvent;
import com.babgo.domain.store.StoreNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEventHandler {

    private final StoreNotificationPort port;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StoreEvent.StatusChanged event) {
        try {
            port.notifyOrderStatusChanged(event.userId(), event.orderId(),event.status(),event.message());
        } catch (Exception e) {
            log.warn("notify failed: userId={}, orderId={} , message={}", event.userId(), event.orderId(), e.getMessage());
        }
    }
}
