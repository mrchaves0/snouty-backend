package com.snouty.api.dto.reporte;

import jakarta.validation.constraints.NotNull;

public record ValidarReporteRequest(
        @NotNull(message = "Debe indicar si el reporte es aprobado (true) o rechazado (false)")
        Boolean aprobado,

        String motivoRechazo
) {}