package com.example.incidenciacli.repository;

import com.example.incidenciacli.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByProfesionalContainingAndCosteGreaterThanEqual(String profesionalRecomendado, Double coste);
}