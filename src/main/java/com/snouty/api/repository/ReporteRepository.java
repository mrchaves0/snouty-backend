package com.snouty.api.repository;

import com.snouty.api.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    @Query(value = "SELECT * FROM reportes r WHERE " +
                   "(:especie IS NULL OR r.especie = CAST(:especie AS VARCHAR)) AND " +
                   "(:estado IS NULL OR r.estado = CAST(:estado AS VARCHAR)) AND " +
                   "(:prioridad IS NULL OR r.prioridad = CAST(:prioridad AS VARCHAR)) " +
                   "ORDER BY r.fecha_creacion DESC", 
           nativeQuery = true)
    List<Reporte> listarConFiltrosOpcionales(
        @Param("especie") String especie,
        @Param("estado") String estado,
        @Param("prioridad") String prioridad
    );
}
