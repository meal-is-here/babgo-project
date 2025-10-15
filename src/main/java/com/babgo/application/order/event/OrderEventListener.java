package com.babgo.application.order.event;

import com.babgo.repository.redis.order.OrderCancelRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderCancelRedisRepository paymentCancelRedisRepository;

    //오더 생성이 커밋이 완료 된 이후에 이벤트를 통해 레디스에 ttl 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {
        paymentCancelRedisRepository.open(e.orderId());
    }
}
