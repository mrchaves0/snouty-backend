package com.snouty.api.repository;

import com.snouty.api.entity.Animal;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("""
            SELECT a FROM Animal a
            WHERE (:especie IS NULL OR a.especie = :especie)
              AND (:estadoAdopcion IS NULL OR a.estadoAdopcion = :estadoAdopcion)
            ORDER BY a.id DESC
            """)
    List<Animal> findWithFilters(
            @Param("especie") Especie especie,
            @Param("estadoAdopcion") EstadoAdopcion estadoAdopcion
    );

    boolean existsByReporteOrigenId(Long reporteOrigenId);

    Optional<Animal> findByReporteOrigenId(Long reporteOrigenId);

    long countByEspecieAndEstadoAdopcion(Especie especie, EstadoAdopcion estadoAdopcion);

    long countByEstadoAdopcion(EstadoAdopcion estadoAdopcion);
}