package com.example.incidenciacli.service;

import com.example.incidenciacli.model.IncidenciaAI;

public interface AIService {
    IncidenciaAI infereIncident(String incidenciaDescription);
}
