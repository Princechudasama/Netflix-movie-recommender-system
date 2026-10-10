package com.prince.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HuggingFaceConfig {

    @Bean
    public RestClient huggingFaceRestClient(
            @Value("${huggingface.api.url}") String apiUrl,
            @Value("${huggingface.api.key}") String apiKey) {

        return RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
