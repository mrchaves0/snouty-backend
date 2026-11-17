package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.TipoEvidencia;
import java.time.LocalDateTime;

public record EvidenciaResponse(
        Long id,
        String urlFoto,
        TipoEvidencia tipo,
        LocalDateTime fecha,
        String descripcion
) {}