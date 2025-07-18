package com.example.incidenciacli.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    ChatClient chatClient (ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    OllamaOptions imageRequestOptions () {
       return OllamaOptions.builder().model("llava-llama3").temperature(0.2d).format(null).build();
    }

    @Bean
    OllamaOptions textRequestOptions () {
        return OllamaOptions.builder().model("llama3.2").temperature(0.2d).format("json").build();
    }
}
