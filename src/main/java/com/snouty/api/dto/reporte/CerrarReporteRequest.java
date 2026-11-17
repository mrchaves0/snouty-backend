package com.snouty.api.dto.reporte;

import jakarta.validation.constraints.NotBlank;

public record CerrarReporteRequest(
        @NotBlank(message = "La observación final del rescate es obligatoria")
        String observacionFinal,

        @NotBlank(message = "La URL de la evidencia fotográfica de cierre es obligatoria")
        String urlFotoCierre,

        @NotBlank(message = "La descripción de la evidencia de cierre es obligatoria")
        String descripcionEvidencia
) {}