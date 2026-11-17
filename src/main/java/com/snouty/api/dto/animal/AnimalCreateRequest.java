package com.snouty.api.dto.animal;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.SexoAnimal;
import com.snouty.api.entity.enums.TamanoAnimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnimalCreateRequest(
        @NotNull(message = "El ID del reporte de origen es obligatorio")
        Long reporteOrigenId,

        Especie especie,

        @NotBlank(message = "El nombre provisional es obligatorio")
        String nombreProvisional,

        @NotBlank(message = "La edad aproximada es obligatoria")
        String edadAproximada,

        @NotNull(message = "El sexo del animal es obligatorio")
        SexoAnimal sexo,

        @NotNull(message = "El tamaño del animal es obligatorio")
        TamanoAnimal tamano,

        @NotBlank(message = "La descripción del animal es obligatoria")
        String descripcion,

        @NotBlank(message = "La URL de la fotografía es obligatoria")
        String urlFoto
) {}