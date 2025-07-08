package com.example.incidenciacli.service;

import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidenciaServiceImpl implements IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Override
    public List<Incidencia> getIncidencias(String tipo, Double costeMinimo) {
        return incidenciaRepository.findByTipoAndCosteGreaterThanEqual(tipo, costeMinimo);
    }

    @Override
    public List<Incidencia> getAllIncidencias() {
        return incidenciaRepository.findAll();
    }

    @Override
    public void deleteIncidencia(Long id) {
        incidenciaRepository.deleteById(id);
    }
}
