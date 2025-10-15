package com.babgo.repository.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
public record PaymentProps (
        String successBaseUrl,
        String failBaseUrl,
        String cancelUrl,
        String refundUrl,
        String webhookUrl
) {
}
