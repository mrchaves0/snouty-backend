package com.snouty.api.dto.estadistica;

import java.util.Map;

public record EstadisticasResponse(
        long totalReportes,
        long reportesPerros,
        long reportesGatos,
        long rescatesExitosos,
        Map<String, Long> reportesPorEstado,
        long totalAnimalesRegistrados,
        long perrosDisponiblesAdopcion,
        long gatosDisponiblesAdopcion,
        long animalesAdoptados,
        long solicitudesAdopcionPendientes,
        long solicitudesAdopcionAprobadas
) {}