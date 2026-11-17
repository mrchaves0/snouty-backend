package com.snouty.api.dto.animal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RevisionVeterinariaCreateRequest(
        @NotBlank(message = "El diagnóstico clínico es obligatorio")
        String diagnostico,

        @NotBlank(message = "Las observaciones y tratamientos son obligatorios")
        String observaciones,

        @NotNull(message = "Debe especificar si el animal está apto para adopción (true/false)")
        Boolean aptoAdopcion,

        Boolean publicarDisponibleInmediatamente
) {}