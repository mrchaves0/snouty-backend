package com.snouty.api.repository;

import com.snouty.api.entity.Evidencia;
import com.snouty.api.entity.enums.TipoEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    List<Evidencia> findByReporteIdOrderByFechaAsc(Long reporteId);
    boolean existsByReporteIdAndTipo(Long reporteId, TipoEvidencia tipo);
}