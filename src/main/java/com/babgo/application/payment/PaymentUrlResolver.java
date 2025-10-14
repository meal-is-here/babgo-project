package com.babgo.application.payment;

import com.babgo.domain.payment.Payment;
import com.babgo.global.config.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentUrlResolver {

    private final PaymentProperties props;

    public String successUrl(Payment Payment) {
        return props.successBaseUrl() + "?orderId=" + Payment.getOrderId();
    }
    public String failUrl(Payment p) {
        return props.failBaseUrl() + "?orderId=" + p.getOrderId();
    }
    public String webhookUrl() {
        return props.webhookUrl();
    }
}
