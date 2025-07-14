package com.example.incidenciacli.tool;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

@Log4j2
public class IncidentCostTool {

    private final VectorStore vectorStore;

    public IncidentCostTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(description = "IncidentCostTool: Calcular el coste de una incidencia dado un concepto y un producto")
    public Double calcularCosto(String concepto, String producto) {

        System.out.println("✅ ¡HERRAMIENTA 'calcularCosto' EJECUTADA! ->Concepto: " + concepto + " ->Producto: " + producto);
        var searchRequest = SearchRequest.builder().query(concepto).topK(1).build();
        var searchResults = vectorStore.similaritySearch(searchRequest);

        if (searchResults != null && !searchResults.isEmpty()) {
            Document bestMatch = searchResults.get(0);
            log.info("✅ ¡Resultado encontrado!");
            log.info("   - Concepto más parecido: '{}'", bestMatch.getText());
            log.info("   - ID (metadata): {}", bestMatch.getMetadata().get("sql_id"));
            log.info("   - Precio (metadata): {}", bestMatch.getMetadata().get("precio"));
            return (Double) bestMatch.getMetadata().get("precio");
        }

        // Cambia el valor a uno que no pueda ser alucinado
        return Double.valueOf(0.0);
    }
}
