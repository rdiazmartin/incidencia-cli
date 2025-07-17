package com.example.incidenciacli.service;

import com.example.incidenciacli.model.IncidenciaAI;

public interface AIService {
    public IncidenciaAI infereIncident(String incidenciaDescription);
    public String analyzeImage(byte[] imageBytes);
}
