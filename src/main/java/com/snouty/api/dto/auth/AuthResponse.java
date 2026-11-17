package com.snouty.api.dto.auth;

import com.snouty.api.entity.enums.RolUsuario;

public record AuthResponse(
        String token,
        Long userId,
        String nombre,
        String correo,
        RolUsuario rol
) {}