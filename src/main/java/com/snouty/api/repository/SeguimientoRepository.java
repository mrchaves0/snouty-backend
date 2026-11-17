package com.snouty.api.repository;

import com.snouty.api.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
    List<Seguimiento> findByReporteIdOrderByFechaAsc(Long reporteId);
    long countByReporteId(Long reporteId);
    long countByReporteIdAndUsuarioId(Long reporteId, Long usuarioId);
}