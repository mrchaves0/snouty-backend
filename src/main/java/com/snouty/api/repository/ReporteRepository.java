package com.snouty.api.repository;

import com.snouty.api.entity.Reporte;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.PrioridadReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    @Query("SELECT r FROM Reporte r WHERE " +
           "(:#{#especie} IS NULL OR r.especie = :especie) AND " +
           "(:#{#estado} IS NULL OR r.estado = :estado) AND " +
           "(:#{#prioridad} IS NULL OR r.prioridad = :prioridad) AND " +
           "(:#{#fechaDesde} IS NULL OR r.fechaCreacion >= :fechaDesde) AND " +
           "(:#{#fechaHasta} IS NULL OR r.fechaCreacion <= :fechaHasta) " +
           "ORDER BY r.fechaCreacion DESC")
    List<Reporte> findWithFilters(
            @Param("especie") Especie especie,
            @Param("estado") EstadoReporte estado,
            @Param("prioridad") PrioridadReporte prioridad,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );

    @Query("SELECT r FROM Reporte r WHERE " +
           "(:#{#especie} IS NULL OR r.especie = :especie) AND " +
           "(:#{#estado} IS NULL OR r.estado = :estado) AND " +
           "(:#{#prioridad} IS NULL OR r.prioridad = :prioridad) " +
           "ORDER BY r.fechaCreacion DESC")
    List<Reporte> listarConFiltrosOpcionales(
            @Param("especie") Especie especie,
            @Param("estado") EstadoReporte estado,
            @Param("prioridad") PrioridadReporte prioridad
    );

    long countByEspecie(Especie especie);

    long countByEstado(EstadoReporte estado);
}