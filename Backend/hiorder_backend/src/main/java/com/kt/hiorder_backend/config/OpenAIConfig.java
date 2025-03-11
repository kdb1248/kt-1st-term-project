package com.kt.hiorder_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.url}")
    private String openAiUrl;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
            .baseUrl(openAiUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}

