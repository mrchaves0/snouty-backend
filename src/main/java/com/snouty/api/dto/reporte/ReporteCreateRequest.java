package com.snouty.api.dto.reporte;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.PrioridadReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReporteCreateRequest(
        @NotNull(message = "La especie (PERRO o GATO) es obligatoria")
        Especie especie,

        @NotBlank(message = "La descripción del hallazgo es obligatoria")
        String descripcion,

        @NotNull(message = "La prioridad es obligatoria")
        PrioridadReporte prioridad,

        @NotNull(message = "La latitud es obligatoria")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        Double longitud,

        @NotBlank(message = "La dirección de referencia es obligatoria")
        String direccionReferencia,

        String urlFotoInicial,
        String descripcionFotoInicial
) {}