package com.example.incidenciacli.config;

import com.example.incidenciacli.model.IncidenciaAI;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public BeanOutputParser<IncidenciaAI> incidenciaAIBeanOutputParser() {
        return new BeanOutputParser<>(IncidenciaAI.class);
    }
}
