package com.kt.hiorder_backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class ChatCompletionResponse {
    private String id;
    private String object;
    private Long created;
    private List<Choice> choices;
    private Usage usage;

    @Data
    public static class Choice {
        private int index;
        private ChatMessage message;
        private String finish_reason;
    }

    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}

