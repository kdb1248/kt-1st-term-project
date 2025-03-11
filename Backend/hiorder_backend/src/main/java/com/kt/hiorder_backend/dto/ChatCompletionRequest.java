package com.kt.hiorder_backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatCompletionRequest {
    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens; 

    // ...
}

