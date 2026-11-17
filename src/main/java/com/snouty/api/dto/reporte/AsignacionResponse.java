package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.EstadoAsignacion;
import java.time.LocalDateTime;

public record AsignacionResponse(
        Long id,
        Long voluntarioId,
        String voluntarioNombre,
        String voluntarioTelefono,
        LocalDateTime fechaAsignacion,
        EstadoAsignacion estado
) {}