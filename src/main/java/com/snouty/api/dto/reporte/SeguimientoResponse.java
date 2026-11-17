package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.EstadoReporte;
import java.time.LocalDateTime;

public record SeguimientoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String usuarioRol,
        LocalDateTime fecha,
        EstadoReporte estadoNuevo,
        String observacion,
        String urlEvidencia
) {}