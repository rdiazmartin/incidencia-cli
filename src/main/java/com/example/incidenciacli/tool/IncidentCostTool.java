package com.example.incidenciacli.tool;

import org.springframework.ai.tool.annotation.Tool;

public class IncidentCostTool {

    @Tool(description = "IncidentCostTool: Calcular el coste de una incidencia dado un concepto y un producto")
    public CostoIncidenciaResponse calcularCosto(CostoIncidenciaRequest request) {
        System.out.println("✅ ¡HERRAMIENTA 'calcularCosto' EJECUTADA! ->Concepto: " + request.concepto + " ->Producto: " + request.producto);
        // Cambia el valor a uno que no pueda ser alucinado
        return new CostoIncidenciaResponse(999.99);
    }

    public record CostoIncidenciaRequest(String concepto, String producto) {
    }

    public record CostoIncidenciaResponse(Double costo) {
    }
}
