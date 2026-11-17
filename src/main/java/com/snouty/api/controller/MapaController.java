package com.snouty.api.controller;

import com.snouty.api.dto.reporte.MarcadorMapaResponse;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mapa")
@RequiredArgsConstructor
@Tag(name = "3. Mapa de Reportes", description = "Consulta optimizada de marcadores georreferenciados por especie y estado")
public class MapaController {

    private final ReporteService reporteService;

    @GetMapping("/reportes")
    @Operation(summary = "Obtener marcadores georreferenciados para el mapa interactivo con filtro por especie y estado")
    public ResponseEntity<List<MarcadorMapaResponse>> obtenerMarcadores(
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) EstadoReporte estado
    ) {
        return ResponseEntity.ok(reporteService.obtenerMarcadoresMapa(especie, estado));
    }
}
