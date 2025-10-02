package com.babgo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 *
 * Refresh Token을 Redis에 저장하기 위한 설정입니다.
 * application.yml에서 Redis 연결 정보를 읽어옵니다.
 */
@Configuration
@EnableRedisRepositories  // Redis Repository 활성화
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * TODO: Redis 연결 팩토리를 생성하는 Bean을 작성해야 합니다
     * - LettuceConnectionFactory를 사용합니다 (Lettuce는 Redis 클라이언트)
     * - host와 port를 설정합니다
     * - 이 Bean은 Redis와의 연결을 관리합니다
     *
     * @return RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: RedisTemplate Bean을 생성해야 합니다
     * - RedisTemplate은 Redis 데이터를 저장하고 조회하는 템플릿 클래스입니다
     * - Key와 Value의 직렬화 방식을 StringRedisSerializer로 설정합니다
     * - setConnectionFactory()로 위에서 만든 연결 팩토리를 주입합니다
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 구현 필요
        return null;
    }
}