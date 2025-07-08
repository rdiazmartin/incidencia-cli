package com.example.incidenciacli.model;

public class IncidenciaAI {
    private String tipo;
    private String profesionalRecomendado;
    private Double coste;
    private String descripcion;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getProfesionalRecomendado() {
        return profesionalRecomendado;
    }

    public void setProfesionalRecomendado(String profesionalRecomendado) {
        this.profesionalRecomendado = profesionalRecomendado;
    }

    public Double getCoste() {
        return coste;
    }

    public void setCoste(Double coste) {
        this.coste = coste;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}