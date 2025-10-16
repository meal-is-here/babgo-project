package com.babgo.domain.ai.by_search_recommendation;

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
    private Embedding embedding;

    // Google Gemini Embedding API용 추가
    private String googleProjectId;   // ex: my-project-id
    private String googleLocation;    // ex: us-central1

    @Getter
    @Setter
    public static class Chat {
        private String baseUrl;
        private String completionPath;
        private String model;
    }

    @Getter
    @Setter
    public static class Embedding {
        private String model;
    }
}
