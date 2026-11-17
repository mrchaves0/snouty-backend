package com.snouty.api.dto.animal;

import com.snouty.api.entity.enums.EstadoAdopcion;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoAnimalRequest(
        @NotNull(message = "El nuevo estado de adopción es obligatorio")
        EstadoAdopcion nuevoEstado
) {}