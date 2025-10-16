package com.babgo.repository.ai.config;

import com.babgo.domain.ai.by_search_recommendation.OpenAiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(OpenAiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getChat().getBaseUrl())
                .build();
    }
}