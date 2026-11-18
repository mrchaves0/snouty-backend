package com.snouty.api.dto.usuario;

import com.snouty.api.entity.enums.RolUsuario;

public record VoluntarioResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        RolUsuario rol,
        Boolean activo
) {
}