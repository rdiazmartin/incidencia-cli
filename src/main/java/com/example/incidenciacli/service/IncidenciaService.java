package com.example.incidenciacli.service;

import com.example.incidenciacli.model.Incidencia;
import java.util.List;

public interface IncidenciaService {
    List<Incidencia> getIncidencias(String profesionalRecomendado, Double costeMinimo);
    List<Incidencia> getAllIncidencias();
    void deleteIncidencia(Long id);
}
