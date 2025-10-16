package com.babgo.application.payment.event;

import com.babgo.application.store.StoreFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final StoreFacade storeFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproveOrder(ApprovedEvent e){storeFacade.acceptedOrder(e.orderId());}
}
