/*
>>>>>>> be758c50e748cf814c552c35bd6935be951a1e47
package com.babgo.repository.ai.config;

import com.babgo.domain.ai.by_search_recommendation.OpenAiProperties;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiEmbeddingModel;

@Configuration
public class OpenAiEmbeddingConfig {

    private final OpenAiProperties openAiProperties;

    public OpenAiEmbeddingConfig(OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;
    }

    @Bean
    public OpenAiEmbeddingModel embeddingModel() {
        // OpenAiApi 객체를 builder로 생성 (ApiKey 처리 내부에서 자동)
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(openAiProperties.getApiKey())
                .baseUrl(openAiProperties.getChat().getBaseUrl())
                .build();

        // EmbeddingModel 생성
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.NONE);
    }
}
*/