package com.snouty.api.controller;

import com.snouty.api.dto.reporte.*;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.PrioridadReporte;
import com.snouty.api.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "2. Reportes y Rescates", description = "Gestión de reportes de perros y gatos, validación, asignación, seguimiento y cierre")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Listar reportes con filtros opcionales (especie, estado, prioridad, fechaDesde, fechaHasta)")
    public ResponseEntity<List<ReporteResumenResponse>> listarReportes(
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) PrioridadReporte prioridad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta
    ) {
        return ResponseEntity.ok(reporteService.listarReportes(especie, estado, prioridad, fechaDesde, fechaHasta));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo reporte georreferenciado de perro o gato en situación de calle")
    public ResponseEntity<ReporteDetalleResponse> crearReporte(
            @Valid @RequestBody ReporteCreateRequest request,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteService.crearReporte(request, usuarioActual));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle completo de un reporte (incluye evidencias, asignaciones y línea de tiempo)")
    public ResponseEntity<ReporteDetalleResponse> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerDetalle(id));
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprobar o rechazar un reporte pendiente (Solo ADMINISTRADOR)")
    public ResponseEntity<ReporteDetalleResponse> validarReporte(
            @PathVariable Long id,
            @Valid @RequestBody ValidarReporteRequest request,
            @AuthenticationPrincipal Usuario admin
    ) {
        return ResponseEntity.ok(reporteService.validarReporte(id, request, admin));
    }

    @PostMapping("/{id}/asignaciones")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Asignar un voluntario a un reporte aprobado (Solo ADMINISTRADOR)")
    public ResponseEntity<ReporteDetalleResponse> asignarVoluntario(
            @PathVariable Long id,
            @Valid @RequestBody AsignarVoluntarioRequest request,
            @AuthenticationPrincipal Usuario admin
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteService.asignarVoluntario(id, request, admin));
    }

    @PostMapping("/{id}/seguimientos")
    @PreAuthorize("hasAnyRole('VOLUNTARIO', 'ADMINISTRADOR')")
    @Operation(summary = "Registrar un avance en la línea de tiempo del rescate (Solo VOLUNTARIO asignado o ADMINISTRADOR)")
    public ResponseEntity<ReporteDetalleResponse> registrarSeguimiento(
            @PathVariable Long id,
            @Valid @RequestBody SeguimientoCreateRequest request,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteService.registrarSeguimiento(id, request, usuarioActual));
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('VOLUNTARIO', 'ADMINISTRADOR')")
    @Operation(summary = "Cerrar el operativo de rescate como RESCATADO adjuntando evidencia obligatoria de cierre")
    public ResponseEntity<ReporteDetalleResponse> cerrarRescate(
            @PathVariable Long id,
            @Valid @RequestBody CerrarReporteRequest request,
            @AuthenticationPrincipal Usuario usuarioActual
    ) {
        return ResponseEntity.ok(reporteService.cerrarRescate(id, request, usuarioActual));
    }
}
