package com.example.incidenciacli.service;

import com.example.incidenciacli.model.IncidenciaAI;
import com.example.incidenciacli.tool.IncidentCostTool;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class AIServiceImpl implements AIService {

    private ChatClient chatClient;

    private Resource promptTemplateResource;

    private Resource promptTemplateResourceImage;

    private VectorStore vectorStore;

    public AIServiceImpl(ChatClient chatClient,
                         VectorStore vectorStore,
                         @Value("classpath:templates/promptTemplate.st") Resource promptTemplateResource,
                         @Value("classpath:templates/promptTemplateImage.st") Resource promptTemplateResourceImage) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.promptTemplateResource = promptTemplateResource;
        this.promptTemplateResourceImage = promptTemplateResourceImage;
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
            String aiResponse = chatClient.prompt(prompt).tools(new IncidentCostTool(vectorStore)).call().content();
            log.info("aiResponse infereIncident:{}", aiResponse);

            return incidenciaAIBeanOutputParser.convert(aiResponse);
        } catch (Exception e) {
            log.error("error processing incident description {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String analyzeImage(byte[] imageBytes) {
        OllamaOptions textRequestOptions = OllamaOptions.builder().model("llama3.2-vision").temperature(0.2d).format(null).build();
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
        Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, imageResource);


        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateResourceImage);
        String promptText = promptTemplate.render(new HashMap<>());

        var userMessage = UserMessage.builder().text(promptText).media(imageMedia).build();

        Prompt finalPrompt = new Prompt(userMessage, textRequestOptions);
        return chatClient.prompt(finalPrompt).call().content();
    }
}
