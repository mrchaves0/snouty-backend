package com.snouty.api.dto.adopcion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudAdopcionCreateRequest(
        @NotNull(message = "El ID del animal a adoptar es obligatorio")
        Long animalId,

        @NotBlank(message = "El motivo y condiciones del hogar son obligatorios")
        String motivo
) {}