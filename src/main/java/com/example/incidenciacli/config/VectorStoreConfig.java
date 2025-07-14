package com.example.incidenciacli.config;

import com.example.incidenciacli.model.CostoConcepto;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Bean
    public VectorStore simpleVectorStore(
            EmbeddingModel embeddingModel,
            // ✅ Aquí está la magia: inyectamos el recurso como parámetro del método.
            @Value("classpath:databases/costoConceptos.csv") Resource csvFile
    ) {
        log.info("Creando y cargando el bean del VectorStore desde un parámetro @Value...");

        List<CostoConcepto> items;
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<CostoConcepto> iterator = csvMapper.readerFor(CostoConcepto.class)
                    .with(schema)
                    .readValues(csvFile.getInputStream());
            items = iterator.readAll();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo CSV", e);
        }

        List<Document> documents = items.stream()
                .map(item -> new Document(
                        item.concepto(),
                        Map.of("sql_id", item.id(), "precio", item.coste_por_unidad())
                ))
                .collect(Collectors.toList());

        log.info("Se cargaron {} documentos desde el CSV.", documents.size());

        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        simpleVectorStore.add(documents);

        log.info("✅ Bean VectorStore creado y cargado exitosamente.");
        return simpleVectorStore;
    }
}