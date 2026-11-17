package com.snouty.api.controller;

import com.snouty.api.dto.animal.*;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.service.AnimalService;
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
@RequestMapping("/api/animales")
@RequiredArgsConstructor
@Tag(name = "4. Animales y Revisiones Veterinarias", description = "Fichas de perros y gatos rescatados, historial médico y disponibilidad de adopción")
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    @Operation(summary = "Consultar catálogo de animales rescatados con filtros opcionales por especie y estado de adopción")
    public ResponseEntity<List<AnimalResponse>> listarAnimales(
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) EstadoAdopcion estadoAdopcion
    ) {
        return ResponseEntity.ok(animalService.listarAnimales(especie, estadoAdopcion));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar la ficha detallada de un animal rescatado")
    public ResponseEntity<AnimalResponse> obtenerAnimal(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.obtenerAnimalPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VOLUNTARIO')")
    @Operation(summary = "Registrar la ficha de un perro o gato vinculado a un reporte RESCATADO")
    public ResponseEntity<AnimalResponse> registrarAnimal(@Valid @RequestBody AnimalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.registrarAnimal(request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @Operation(summary = "Cambiar estado de adopción de un animal (valida requisito de revisión veterinaria apta para pasar a DISPONIBLE)")
    public ResponseEntity<AnimalResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoAnimalRequest request
    ) {
        return ResponseEntity.ok(animalService.cambiarEstadoAdopcion(id, request.nuevoEstado()));
    }

    @GetMapping("/{id}/revisiones")
    @Operation(summary = "Consultar historial de revisiones veterinarias de un animal")
    public ResponseEntity<List<RevisionVeterinariaResponse>> listarRevisiones(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.listarRevisionesPorAnimal(id));
    }

    @PostMapping("/{id}/revisiones")
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Registrar una nueva valoración clínica y definir si es apto para adopción (Solo VETERINARIO)")
    public ResponseEntity<RevisionVeterinariaResponse> registrarRevision(
            @PathVariable Long id,
            @Valid @RequestBody RevisionVeterinariaCreateRequest request,
            @AuthenticationPrincipal Usuario veterinario
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(animalService.registrarRevisionVeterinaria(id, request, veterinario));
    }
}
