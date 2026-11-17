package com.snouty.api.dto.animal;

import java.time.LocalDateTime;

public record RevisionVeterinariaResponse(
        Long id,
        Long animalId,
        String nombreAnimal,
        Long veterinarioId,
        String nombreVeterinario,
        LocalDateTime fecha,
        String diagnostico,
        String observaciones,
        Boolean aptoAdopcion
) {}