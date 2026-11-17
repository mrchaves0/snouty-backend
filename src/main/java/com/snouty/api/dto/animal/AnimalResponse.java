package com.snouty.api.dto.animal;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.entity.enums.SexoAnimal;
import com.snouty.api.entity.enums.TamanoAnimal;
import java.util.List;

public record AnimalResponse(
        Long id,
        Long reporteOrigenId,
        Especie especie,
        String nombreProvisional,
        String edadAproximada,
        SexoAnimal sexo,
        TamanoAnimal tamano,
        String descripcion,
        String urlFoto,
        EstadoAdopcion estadoAdopcion,
        Boolean cuentaConAvalVeterinario,
        List<RevisionVeterinariaResponse> revisiones
) {}