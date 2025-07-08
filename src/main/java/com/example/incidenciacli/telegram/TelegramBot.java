package com.example.incidenciacli.telegram;

import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.model.IncidenciaAI;
import com.example.incidenciacli.repository.IncidenciaRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("classpath:templates/promptTemplate.st")
    private Resource promptTemplateResource;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private BeanOutputParser<IncidenciaAI> incidenciaAIBeanOutputParser;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
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
            String format = incidenciaAIBeanOutputParser.getFormat();
            String promptMessage = promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
            PromptTemplate promptTemplate = new PromptTemplate(promptMessage, Map.of("format", format));
            Prompt prompt = promptTemplate.create(Map.of("incidentDescription", incidentDescription));
            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();

            IncidenciaAI incidenciaAI = incidenciaAIBeanOutputParser.parse(aiResponse);

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
