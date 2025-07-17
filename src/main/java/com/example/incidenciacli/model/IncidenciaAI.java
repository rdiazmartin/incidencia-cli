package com.example.incidenciacli.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaAI {
    private String tipo;
    private String profesional;
    private Double coste;
    private String descripcion;
}