package com.babgo.repository.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai")
public class OpenAiProperties {
    private String apiKey;
    private Chat chat;

    @Getter
    @Setter
    public static class Chat {
        private String baseUrl;
        private String completionPath;
        private String model;
    }
}