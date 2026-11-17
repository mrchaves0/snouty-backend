package com.snouty.api.dto.reporte;

import jakarta.validation.constraints.NotNull;

public record AsignarVoluntarioRequest(
        @NotNull(message = "El ID del voluntario es obligatorio")
        Long voluntarioId
) {}