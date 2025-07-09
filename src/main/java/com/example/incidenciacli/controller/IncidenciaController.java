package com.example.incidenciacli.controller;

import com.example.incidenciacli.model.Incidencia;
import com.example.incidenciacli.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IncidenciaController {

    @Autowired
    private IncidenciaService incidenciaService;

    @GetMapping("/incidencias")
    public List<Incidencia> getIncidencias(@RequestParam String profesionalRecomendado, @RequestParam Double costeMinimo) {
        return incidenciaService.getIncidencias(profesionalRecomendado, costeMinimo);
    }

    @GetMapping("/list")
    public List<Incidencia> getAllIncidencias() {
        return incidenciaService.getAllIncidencias();
    }

    @DeleteMapping("/remove/{id}")
    public void deleteIncidencia(@PathVariable Long id) {
        incidenciaService.deleteIncidencia(id);
    }
}
