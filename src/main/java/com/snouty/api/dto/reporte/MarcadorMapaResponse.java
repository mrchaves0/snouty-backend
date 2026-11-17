package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.PrioridadReporte;
import java.time.LocalDateTime;

public record MarcadorMapaResponse(
        Long reporteId,
        Especie especie,
        EstadoReporte estado,
        PrioridadReporte prioridad,
        Double latitud,
        Double longitud,
        String direccionReferencia,
        String descripcionCorta,
        LocalDateTime fechaCreacion,
        String urlFotoMiniatura,
        String colorHexSugerido
) {}