package com.babgo.application.payment.processor;

import com.babgo.application.payment.PaymentInfo;
import com.babgo.controller.payment.gateway.PaymentGateway;
import com.babgo.domain.payment.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor{

    @Qualifier("fakePaymentGateway")
    private final PaymentGateway PaymentGateway;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CARD;
    }

    @Override
    public void  process(PaymentProcessContext ctx) {
        //멱등키 생성
        String idempotencyKey = "confirm:" + ctx.paymentId();

        PaymentInfo.Confirm input =  PaymentInfo.Confirm.from(idempotencyKey, ctx);
        //요청
        PaymentGateway.confirm(input);
    }
}
