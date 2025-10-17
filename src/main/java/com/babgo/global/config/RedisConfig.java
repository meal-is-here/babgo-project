package com.babgo.global.config;

import com.babgo.repository.redis.order.OrderRedisProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.babgo.repository.redis.order.OrderExpiredListener;

/**
 * Redis 설정 - Refresh Token 저장소 & Keyspace Notification
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OrderRedisProps.class)
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    // Redis 연결 팩토리 (Lettuce 사용)
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        
        // 빈 값이 아닐 때만 password 설정
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        try {
            factory.getConnection().serverCommands()
                .setConfig("notify-keyspace-events", "Ex");
        } catch (Exception e) {
            log.error("Redis Keyspace Notification 설정 실패", e);
        }

        return factory;
    }

    // RedisTemplate 설정 (Key/Value 모두 String 직렬화)
    @Bean(name = "authRedisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean(name = "orderRedisTemplate")
    public RedisTemplate<String, String> orderRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        StringRedisSerializer s = new StringRedisSerializer();
        redisTemplate.setKeySerializer(s);
        redisTemplate.setValueSerializer(s);
        redisTemplate.setHashKeySerializer(s);
        redisTemplate.setHashValueSerializer(s);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean(name = "searchRedisTemplate")
    public RedisTemplate<String, String> searchRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        StringRedisSerializer s = new StringRedisSerializer();
        redisTemplate.setKeySerializer(s);
        redisTemplate.setValueSerializer(s);
        redisTemplate.setHashKeySerializer(s);
        redisTemplate.setHashValueSerializer(s);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            OrderExpiredListener orderExpiredListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Keyspace notification 구독
        container.addMessageListener(
                orderExpiredListener,
                new PatternTopic("__keyevent@*__:expired")
        );

        return container;
    }
}