package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.PrioridadReporte;
import java.time.LocalDateTime;

public record ReporteResumenResponse(
        Long id,
        Especie especie,
        String descripcion,
        LocalDateTime fechaCreacion,
        EstadoReporte estado,
        PrioridadReporte prioridad,
        Long ciudadanoId,
        String ciudadanoNombre,
        Double latitud,
        Double longitud,
        String direccionReferencia,
        String urlFotoPrincipal
) {}