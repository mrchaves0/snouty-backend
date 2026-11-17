package com.snouty.api.controller;

import com.snouty.api.dto.adopcion.ResolverSolicitudAdopcionRequest;
import com.snouty.api.dto.adopcion.SolicitudAdopcionCreateRequest;
import com.snouty.api.dto.adopcion.SolicitudAdopcionResponse;
import com.snouty.api.entity.Usuario;
import com.snouty.api.service.AdopcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adopciones/solicitudes")
@RequiredArgsConstructor
@Tag(name = "5. Adopciones", description = "Recepción y decisión administrativa de solicitudes de adopción")
public class AdopcionController {

    private final AdopcionService adopcionService;

    @GetMapping
    @Operation(summary = "Consultar solicitudes de adopción (Todas para Admin/Voluntario, o propias para Ciudadano)")
    public ResponseEntity<List<SolicitudAdopcionResponse>> listarSolicitudes(
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.ok(adopcionService.listarSolicitudes(usuarioActual));
    }

    @PostMapping
    @Operation(summary = "Enviar una solicitud de adopción para un perro o gato disponible")
    public ResponseEntity<SolicitudAdopcionResponse> crearSolicitud(
            @Valid @RequestBody SolicitudAdopcionCreateRequest request,
            @AuthenticationPrincipal Usuario ciudadano
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adopcionService.crearSolicitud(request, ciudadano));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprobar o rechazar una solicitud de adopción con observación (Solo ADMINISTRADOR)")
    public ResponseEntity<SolicitudAdopcionResponse> resolverSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody ResolverSolicitudAdopcionRequest request,
            @AuthenticationPrincipal Usuario admin
    ) {
        return ResponseEntity.ok(adopcionService.resolverSolicitud(id, request, admin));
    }
}
