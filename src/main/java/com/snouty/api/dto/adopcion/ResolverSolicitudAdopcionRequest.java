package com.snouty.api.dto.adopcion;

import com.snouty.api.entity.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolverSolicitudAdopcionRequest(
        @NotNull(message = "El nuevo estado (APROBADA o RECHAZADA) es obligatorio")
        EstadoSolicitud estado,

        @NotBlank(message = "La observación administrativa es obligatoria")
        String observacionAdmin
) {}