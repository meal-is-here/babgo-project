package com.babgo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties (
        String successBaseUrl,
        String failBaseUrl,
        String webhookUrl
) {
}
