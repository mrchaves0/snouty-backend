package com.snouty.api.controller;

import com.snouty.api.dto.estadistica.EstadisticasResponse;
import com.snouty.api.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Tag(name = "6. Estadísticas e Indicadores", description = "Indicadores generales de reportes, rescates, perros/gatos disponibles y adopciones")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping
    @Operation(summary = "Consultar conteos generales por estado de reportes, rescates exitosos, animales disponibles y adopciones")
    public ResponseEntity<EstadisticasResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(estadisticaService.obtenerEstadisticasGenerales());
    }
}
