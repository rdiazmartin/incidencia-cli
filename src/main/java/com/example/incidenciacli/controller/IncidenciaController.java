package com.example.incidenciacli.controller;

import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class IncidenciaController {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @GetMapping("/incidencias")
    public List<Incidencia> getIncidencias(@RequestParam String tipo, @RequestParam Double costeMinimo) {
        return incidenciaRepository.findByTipoAndCosteGreaterThanEqual(tipo, costeMinimo);
    }

    @GetMapping("/list")
    public List<Incidencia> getAllIncidencias() {
        return incidenciaRepository.findAll();
    }

    @DeleteMapping("/remove/{id}")
    public void deleteIncidencia(@PathVariable Long id) {
        incidenciaRepository.deleteById(id);
    }
}