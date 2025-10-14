package com.babgo.repository.ai.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
public class GoogleAiController {

    private final OpenAiProperties openAiProperties;
    private final RestClient restClient;

    public GoogleAiController(OpenAiProperties openAiProperties, RestClient.Builder builder) {
        this.openAiProperties = openAiProperties;
        String baseUrl = openAiProperties.getChat().getBaseUrl();
        String baseUrlWithSlash = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.restClient = builder.baseUrl(baseUrlWithSlash).build();
    }

    @GetMapping("/models")
    public Map<String, Object> getModels() {
        ResponseEntity<Map> response = restClient.get()
                .uri("v1beta/openai/models")
                .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                .retrieve()
                .toEntity(Map.class);
        return response.getBody();
    }
}
