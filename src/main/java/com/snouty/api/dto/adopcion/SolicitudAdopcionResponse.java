package com.snouty.api.dto.adopcion;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoSolicitud;
import java.time.LocalDateTime;

public record SolicitudAdopcionResponse(
        Long id,
        Long animalId,
        String nombreAnimal,
        Especie especieAnimal,
        String urlFotoAnimal,
        Long ciudadanoId,
        String nombreCiudadano,
        String correoCiudadano,
        String telefonoCiudadano,
        LocalDateTime fecha,
        String motivo,
        EstadoSolicitud estado,
        String observacionAdmin
) {}