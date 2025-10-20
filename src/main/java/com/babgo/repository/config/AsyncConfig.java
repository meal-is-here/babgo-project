package com.babgo.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "paymentExecutor")
    public ThreadPoolTaskExecutor paymentExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("payment-");
        executor.initialize();
        return executor;
    }


    @Bean(name = "storeExecutor")
    public ThreadPoolTaskExecutor storeExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("store-");
        executor.initialize();
        return executor;
    }

}
