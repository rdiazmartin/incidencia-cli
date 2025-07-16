package com.example.incidenciacli.service;

import com.example.incidenciacli.model.IncidenciaAI;
import com.example.incidenciacli.tool.IncidentCostTool;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Service
public class AIServiceImpl implements AIService {

    private Resource promptTemplateResource;

    private ChatModel chatModel;

    private VectorStore vectorStore;

    public AIServiceImpl (ChatModel chatModel, VectorStore vectorStore,@Value("classpath:templates/promptTemplate.st") Resource promptTemplateResource) {
       this.chatModel = chatModel;
       this.vectorStore = vectorStore;
        this.promptTemplateResource = promptTemplateResource;
    }

    @Override
    public IncidenciaAI infereIncident(String incidentDescription) {

        try {
            var incidenciaAIBeanOutputParser = new BeanOutputConverter<>(IncidenciaAI.class);
            String format = incidenciaAIBeanOutputParser.getFormat();
            log.info("format: {}", format);
            String promptMessage = promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
            Prompt prompt = promptTemplate.create(Map.of("incidentDescription", incidentDescription, "format", format));
            String aiResponse = ChatClient.create(chatModel).prompt(prompt).tools(new IncidentCostTool(vectorStore)).call().content();
            log.info("aiResponse:{}", aiResponse);

            return incidenciaAIBeanOutputParser.convert(aiResponse);
        } catch (Exception e) {
            log.error("error processing incident description {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
