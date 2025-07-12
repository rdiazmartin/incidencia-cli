package com.example.incidenciacli.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// El @JsonPropertyOrder asegura que Jackson mapee las columnas en este orden
@JsonPropertyOrder({"id", "concepto", "coste_por_unidad"})
public record CostoConcepto(int id, String concepto, double coste_por_unidad) {
}

