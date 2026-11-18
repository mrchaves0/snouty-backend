package com.snouty.api.controller;

import com.snouty.api.dto.reporte.*;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.PrioridadReporte;
import com.snouty.api.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes y Rescates", description = "Gestion completa de reportes georreferenciados, asignacion de voluntarios, seguimientos y cierre de rescate con ingreso automatico al refugio")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    @Operation(summary = "Crear nuevo reporte de animal en calle con ubicacion y fotografia inicial")
    public ResponseEntity<ReporteDetalleResponse> crearReporte(
            @Valid @RequestBody ReporteCreateRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.crearReporte(request, usuario));
    }

    @GetMapping
    @Operation(summary = "Listar reportes con filtros opcionales (especie, estado, prioridad, rango de fechas)")
    public ResponseEntity<List<ReporteResumenResponse>> listarReportes(
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) PrioridadReporte prioridad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta
    ) {
        return ResponseEntity.ok(reporteService.listarReportes(especie, estado, prioridad, fechaDesde, fechaHasta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle completo de un reporte, evidencias, asignaciones y linea de tiempo")
    public ResponseEntity<ReporteDetalleResponse> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerDetalle(id));
    }

    @PatchMapping("/{id}/validacion")
    @Operation(summary = "Validar reporte (Aprobar o Rechazar con motivo obligatorio) - Rol ADMINISTRADOR")
    public ResponseEntity<ReporteDetalleResponse> validarReporte(
            @PathVariable Long id,
            @Valid @RequestBody ValidarReporteRequest request,
            @AuthenticationPrincipal Usuario admin
    ) {
        return ResponseEntity.ok(reporteService.validarReporte(id, request, admin));
    }

    @PostMapping("/{id}/asignaciones")
    @Operation(summary = "Asignar voluntario activo a un reporte - Rol ADMINISTRADOR / VOLUNTARIO")
    public ResponseEntity<ReporteDetalleResponse> asignarVoluntario(
            @PathVariable Long id,
            @Valid @RequestBody AsignarVoluntarioRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(reporteService.asignarVoluntario(id, request, usuario));
    }

    @PostMapping("/{id}/seguimientos")
    @Operation(summary = "Registrar avance en la bitacora del rescate - Rol VOLUNTARIO o ADMINISTRADOR")
    public ResponseEntity<ReporteDetalleResponse> registrarSeguimiento(
            @PathVariable Long id,
            @Valid @RequestBody SeguimientoCreateRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.registrarSeguimiento(id, request, usuario));
    }

    @RequestMapping(value = "/{id}/cerrar", method = {RequestMethod.POST, RequestMethod.PATCH})
    @Operation(
            summary = "Cerrar operativo como RESCATADO e ingresar automaticamente el animal al refugio",
            description = "Cierra el reporte como RESCATADO con evidencia fotografica y crea automaticamente el registro en la tabla animales con estado EN_RECUPERACION para valoracion veterinaria."
    )
    public ResponseEntity<ReporteDetalleResponse> cerrarRescate(
            @PathVariable Long id,
            @Valid @RequestBody CerrarReporteRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(reporteService.cerrarRescate(id, request, usuario));
    }
}