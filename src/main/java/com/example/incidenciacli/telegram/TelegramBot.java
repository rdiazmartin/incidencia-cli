package com.example.incidenciacli.telegram;

import com.example.incidenciacli.config.TelegramConfig;
import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.service.AIService;
import com.example.incidenciacli.service.IncidenciaService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

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
        log.info("update recibido");
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                log.info("ok has message and text");
                String messageText = update.getMessage().getText();

                if (messageText.startsWith("/incidencia")) {
                    log.info("incidencia recibida con exito para analizar texto");
                    String incidentDescription = messageText.substring("/incidencia ".length());
                    System.out.println("debug: inicidencia: " + incidentDescription);
                    processIncident(incidentDescription, chatId);

                }
            } else if (update.getMessage().hasPhoto()) {
                log.info("incidencia recibida con exito para analizar imagen");
                Message message = update.getMessage();
                List<PhotoSize> photos = message.getPhoto();

                log.info("imagenes recibidas...");
                photos.forEach(im -> log.info("imagen: {}x{} ", im.getWidth(), im.getHeight()));
                PhotoSize photo = photos.stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);

                if (photo != null) {
                    byte[] imageBytes = fetchImageBytes(photo);
                    try {
                        log.info("ok going to analyse image");
                        String aiResponse = aiService.analyzeImage(imageBytes);
                        log.info("ok image analyzed: response of the model: {}", aiResponse);
                        processIncident(aiResponse, chatId);
                    } catch (Exception e) {
                        log.error("error processing image");
                        e.printStackTrace();
                        sendMessage(chatId, "Error al procesar la imagen con la IA.");
                    }

                } // photo! null

            } else { // main
                sendMessage(chatId, "envia una incidenciaImg <image>");
            }
        }
    }

    private void processIncident(String incidentDescription, long chatId) {
        try {

            var incidenciaAI = aiService.infereIncident(incidentDescription);
            if (incidenciaAI != null) {
                Incidencia incidencia = Incidencia.builder()
                        .tipo(incidenciaAI.getTipo())
                        .profesional(incidenciaAI.getProfesional())
                        .coste(incidenciaAI.getCoste())
                        .descripcion(incidenciaAI.getDescripcion()).build();

                incidenciaService.saveIncidencia(incidencia);

                sendMessage(chatId, "Incidencia registrada con éxito:\n" +
                        "Tipo: " + incidencia.getTipo() + "\n" +
                        "Profesional: " + incidencia.getProfesional() + "\n" +
                        "Coste: " + incidencia.getCoste() + "\n" +
                        "Descripción: " + incidencia.getDescripcion());

            } else {
                log.error("model returned null for this incident: {}", incidentDescription);
                sendMessage(chatId, "the aiservice returned null for this incident " + incidentDescription);
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

    private byte[] fetchImageBytes(PhotoSize photo) {

        String fileId = photo.getFileId();
        byte[] imageBytes = null;
        int maxRetries = 3; // Número máximo de reintentos
        long delay = 1000;  // Tiempo de espera en milisegundos (1 segundo)

        for (int i = 0; i < maxRetries; i++) {
            try {
                GetFile getFileRequest = new GetFile(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);
                String downloadUrl = file.getFileUrl(getBotToken());

                log.info("Intento {}/{}: descargando imagen desde {}", (i + 1), maxRetries, downloadUrl);

                // Usar HttpClient para la descarga (más moderno que URL.openStream())
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(downloadUrl))
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                System.out.println("response: " + response.toString());
                if (response.statusCode() == 200) {
                    imageBytes = response.body();
                    break; // Salir del bucle si la descarga es exitosa
                } else {
                    log.warn("Fallo en la descarga, código de estado: {}", response.statusCode());
                }

            } catch (Exception e) {
                log.error("Error en el intento {} de descarga: {}", (i + 1), e.getMessage());
            }

            if (i < maxRetries - 1) { // No esperar después del último intento
                try {
                    log.info("Esperando {} ms antes del siguiente intento...", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("El hilo fue interrumpido.", ie);
                    break;
                }
            }
        }
        if (imageBytes == null) {
            log.error("No se pudo descargar la imagen después de {} intentos.", maxRetries);
        }

        return imageBytes;
    }
}
