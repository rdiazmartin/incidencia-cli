package com.example.incidenciacli.telegram;

import com.example.incidenciacli.config.TelegramConfig;
import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.model.IncidenciaAI;
import com.example.incidenciacli.repository.IncidenciaRepository;
import com.example.incidenciacli.service.AIService;
import com.example.incidenciacli.service.IncidenciaService;
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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    TelegramConfig telegramConfig;

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    AIService aiService;

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

            var incidenciaAI = aiService.infereIncident(incidentDescription);
            if (incidenciaAI != null) {
                Incidencia incidencia = Incidencia.builder()
                        .tipo(incidenciaAI.getTipo())
                        .profesionalRecomendado(incidenciaAI.getProfesionalRecomendado())
                        .coste(incidenciaAI.getCoste())
                        .descripcion(incidenciaAI.getDescripcion()).build();
                
                incidenciaService.saveIncidencia(incidencia);

                sendMessage(chatId, "Incidencia registrada con éxito:\n" +
                        "Tipo: " + incidencia.getTipo() + "\n" +
                        "Profesional: " + incidencia.getProfesionalRecomendado() + "\n" +
                        "Coste: " + incidencia.getCoste() + "\n" +
                        "Descripción: " + incidencia.getDescripcion());

            } else {
                log.error("model returned null for this incident: {}", incidentDescription);
                sendMessage(chatId, "the model returned null for this incident " + incidentDescription);
            }
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
