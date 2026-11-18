package com.snouty.api.controller;

import com.snouty.api.dto.usuario.VoluntarioResponse;
import com.snouty.api.entity.enums.RolUsuario;
import com.snouty.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios y Voluntarios", description = "Consulta de voluntarios disponibles y directorio de usuarios para asignacion de operativos")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/voluntarios")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ADMINISTRADOR', 'ROLE_VOLUNTARIO', 'VOLUNTARIO')")
    @Operation(
            summary = "Listar voluntarios disponibles",
            description = "Devuelve la lista de usuarios con rol VOLUNTARIO activos en la base de datos para asignacion de rescates desde el panel de Administrador."
    )
    public ResponseEntity<List<VoluntarioResponse>> listarVoluntarios() {
        return ResponseEntity.ok(usuarioService.listarVoluntariosActivos());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar usuarios filtrados opcionalmente por rol",
            description = "Permite al Administrador consultar usuarios registrados filtrando opcionalmente por rol (CIUDADANO, VOLUNTARIO, VETERINARIO, ADMINISTRADOR)."
    )
    public ResponseEntity<List<VoluntarioResponse>> listarUsuarios(
            @RequestParam(required = false) RolUsuario rol
    ) {
        return ResponseEntity.ok(usuarioService.listarUsuariosPorRol(rol));
    }
}