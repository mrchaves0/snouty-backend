package com.snouty.api.repository;

import com.snouty.api.entity.Asignacion;
import com.snouty.api.entity.enums.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    List<Asignacion> findByReporteId(Long reporteId);
    List<Asignacion> findByVoluntarioIdOrderByFechaAsignacionDesc(Long voluntarioId);
    boolean existsByReporteIdAndVoluntarioIdAndEstado(Long reporteId, Long voluntarioId, EstadoAsignacion estado);
    Optional<Asignacion> findByReporteIdAndVoluntarioId(Long reporteId, Long voluntarioId);
}