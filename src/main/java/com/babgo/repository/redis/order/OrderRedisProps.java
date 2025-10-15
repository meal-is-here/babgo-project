package com.babgo.repository.redis.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.redis")
public record OrderRedisProps (
        String keyPrefix,
        String cancelWindowSuffix,
        int cancelWindowTtlSeconds
){
}
