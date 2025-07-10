package com.example.incidenciacli.telegram;

import com.example.incidenciacli.config.TelegramConfig;
import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.model.IncidenciaAI;
import com.example.incidenciacli.repository.IncidenciaRepository;
import com.example.incidenciacli.tool.IncidentCostTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    TelegramConfig telegramConfig;

    @Value("classpath:templates/promptTemplate.st")
    private Resource promptTemplateResource;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Override
    public String getBotUsername() {
        return telegramConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return telegramConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/incidencia")) {
                String incidentDescription = messageText.substring("/incidencia ".length());
                System.out.println("debug: inicidencia: " + incidentDescription);
                processIncident(incidentDescription, chatId);
            } else {
                sendMessage(chatId, "Hola! Envía una incidencia con el formato: /incidencia [descripción de la incidencia]");
            }
        }
    }

    private void processIncident(String incidentDescription, long chatId) {
        try {
            var incidenciaAIBeanOutputParser = new BeanOutputConverter<>(IncidenciaAI.class);
            String format = incidenciaAIBeanOutputParser.getFormat();
            String promptMessage = promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            PromptTemplate promptTemplate = new PromptTemplate(promptMessage);
            Prompt prompt = promptTemplate.create(Map.of("incidentDescription", incidentDescription, "format", format));
            String aiResponse = ChatClient.create(chatModel).prompt(prompt).tools(new IncidentCostTool()).call().content();

            IncidenciaAI incidenciaAI = incidenciaAIBeanOutputParser.convert(aiResponse);

            Incidencia incidencia = new Incidencia();
            incidencia.setTipo(incidenciaAI.getTipo());
            incidencia.setProfesionalRecomendado(incidenciaAI.getProfesionalRecomendado());
            incidencia.setCoste(incidenciaAI.getCoste());
            incidencia.setDescripcion(incidenciaAI.getDescripcion());

            incidenciaRepository.save(incidencia);

            sendMessage(chatId, "Incidencia registrada con éxito:\n" +
                    "Tipo: " + incidencia.getTipo() + "\n" +
                    "Profesional: " + incidencia.getProfesionalRecomendado() + "\n" +
                    "Coste: " + incidencia.getCoste() + "\n" +
                    "Descripción: " + incidencia.getDescripcion());

        } catch (Exception e) {
            sendMessage(chatId, "Error al procesar la incidencia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
