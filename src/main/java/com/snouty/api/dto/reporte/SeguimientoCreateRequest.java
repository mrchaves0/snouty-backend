package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.EstadoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeguimientoCreateRequest(
        @NotNull(message = "El nuevo estado del reporte es obligatorio")
        EstadoReporte estadoNuevo,

        @NotBlank(message = "La observación del seguimiento es obligatoria")
        String observacion,

        String urlEvidencia
) {}