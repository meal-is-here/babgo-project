package com.babgo.application.payment.port;

import java.util.UUID;

public interface PaymentEndpointResolver {
    String approveUrl(UUID paymentId);
    String cancelUrl(UUID paymentId);
    String refundUrl(UUID paymentId);
    String failUrl(UUID paymentId);
    String webhookUrl();

}
