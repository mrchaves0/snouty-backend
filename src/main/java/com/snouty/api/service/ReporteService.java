package com.snouty.api.service;

import com.snouty.api.dto.reporte.*;
import com.snouty.api.entity.*;
import com.snouty.api.entity.enums.*;
import com.snouty.api.exception.BusinessRuleException;
import com.snouty.api.exception.ResourceNotFoundException;
import com.snouty.api.exception.UnauthorizedOperationException;
import com.snouty.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final AsignacionRepository asignacionRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ReporteDetalleResponse crearReporte(ReporteCreateRequest request, Usuario ciudadanoAutenticado) {
        Usuario autor = (ciudadanoAutenticado != null)
                ? ciudadanoAutenticado
                : usuarioRepository.findByRolAndActivoTrue(RolUsuario.CIUDADANO)
                        .stream()
                        .findFirst()
                        .orElseGet(() -> usuarioRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new BusinessRuleException("No hay usuarios registrados para asociar el reporte.")));

        var reporte = Reporte.builder()
                .especie(request.especie())
                .descripcion(request.descripcion().trim())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoReporte.PENDIENTE)
                .prioridad(request.prioridad())
                .ciudadano(autor)
                .latitud(request.latitud())
                .longitud(request.longitud())
                .direccionReferencia(request.direccionReferencia().trim())
                .build();

        var reporteGuardado = reporteRepository.save(reporte);

        if (request.urlFotoInicial() != null && !request.urlFotoInicial().isBlank()) {
            var descripcionFoto = (request.descripcionFotoInicial() != null && !request.descripcionFotoInicial().isBlank())
                    ? request.descripcionFotoInicial().trim()
                    : "Fotografia inicial del reporte registrada por el ciudadano.";

            var evidenciaInicial = Evidencia.builder()
                    .reporte(reporteGuardado)
                    .urlFoto(request.urlFotoInicial().trim())
                    .tipo(TipoEvidencia.INICIAL)
                    .fecha(LocalDateTime.now())
                    .descripcion(descripcionFoto)
                    .build();

            reporteGuardado.getEvidencias().add(evidenciaRepository.save(evidenciaInicial));
        }

        return mapToDetalleResponse(reporteGuardado);
    }

    @Transactional(readOnly = true)
    public List<ReporteResumenResponse> listarReportes(
            Especie especie,
            EstadoReporte estado,
            PrioridadReporte prioridad,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        return reporteRepository.findWithFilters(especie, estado, prioridad, fechaDesde, fechaHasta)
                .stream()
                .map(this::mapToResumenResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReporteDetalleResponse obtenerDetalle(Long reporteId) {
        var reporte = obtenerEntidadReporte(reporteId);
        return mapToDetalleResponse(reporte);
    }

    @Transactional
    public ReporteDetalleResponse validarReporte(Long reporteId, ValidarReporteRequest request, Usuario admin) {
        if (admin != null && admin.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new UnauthorizedOperationException("Solo un ADMINISTRADOR puede aprobar o rechazar reportes.");
        }

        Usuario autorAdmin = (admin != null)
                ? admin
                : usuarioRepository.findByRolAndActivoTrue(RolUsuario.ADMINISTRADOR)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontro usuario administrador."));

        var reporte = obtenerEntidadReporte(reporteId);

        if (Boolean.TRUE.equals(request.aprobado())) {
            reporte.setEstado(EstadoReporte.APROBADO);
            reporte.setMotivoRechazo(null);

            registrarSeguimientoInterno(
                    reporte,
                    autorAdmin,
                    EstadoReporte.APROBADO,
                    "Reporte validado y aprobado por administracion para despliegue de rescate.",
                    null
            );
        } else {
            if (request.motivoRechazo() == null || request.motivoRechazo().isBlank()) {
                throw new BusinessRuleException("El motivo de rechazo es obligatorio cuando se rechaza un reporte.");
            }
            reporte.setEstado(EstadoReporte.RECHAZADO);
            reporte.setMotivoRechazo(request.motivoRechazo().trim());

            registrarSeguimientoInterno(
                    reporte,
                    autorAdmin,
                    EstadoReporte.RECHAZADO,
                    "Reporte rechazado. Motivo: " + request.motivoRechazo().trim(),
                    null
            );
        }

        return mapToDetalleResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public ReporteDetalleResponse asignarVoluntario(Long reporteId, AsignarVoluntarioRequest request, Usuario usuarioActual) {
        if (usuarioActual != null
                && usuarioActual.getRol() != RolUsuario.ADMINISTRADOR
                && usuarioActual.getRol() != RolUsuario.VOLUNTARIO) {
            throw new UnauthorizedOperationException("Solo un ADMINISTRADOR o VOLUNTARIO puede gestionar asignaciones.");
        }

        var reporte = obtenerEntidadReporte(reporteId);

        var voluntario = usuarioRepository.findById(request.voluntarioId())
                .orElseGet(() -> usuarioRepository.findByRolAndActivoTrue(RolUsuario.VOLUNTARIO)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No existe el voluntario con ID: " + request.voluntarioId())));

        var asignacionExistente = asignacionRepository.findByReporteIdAndVoluntarioId(reporteId, voluntario.getId());
        if (asignacionExistente.isEmpty()) {
            var asignacion = Asignacion.builder()
                    .reporte(reporte)
                    .voluntario(voluntario)
                    .fechaAsignacion(LocalDateTime.now())
                    .estado(EstadoAsignacion.ACTIVA)
                    .build();
            reporte.getAsignaciones().add(asignacionRepository.save(asignacion));
        } else {
            var asig = asignacionExistente.get();
            asig.setEstado(EstadoAsignacion.ACTIVA);
            asignacionRepository.save(asig);
        }

        if (reporte.getEstado() == EstadoReporte.PENDIENTE || reporte.getEstado() == EstadoReporte.APROBADO) {
            reporte.setEstado(EstadoReporte.ASIGNADO);
        }

        Usuario autor = (usuarioActual != null) ? usuarioActual : voluntario;
        registrarSeguimientoInterno(
                reporte,
                autor,
                reporte.getEstado(),
                "Voluntario asignado al operativo de rescate: " + voluntario.getNombre(),
                null
        );

        return mapToDetalleResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public ReporteDetalleResponse registrarSeguimiento(Long reporteId, SeguimientoCreateRequest request, Usuario usuarioActual) {
        var reporte = obtenerEntidadReporte(reporteId);

        if (reporte.getEstado() == EstadoReporte.RECHAZADO || reporte.getEstado() == EstadoReporte.RESCATADO) {
            throw new BusinessRuleException("No se pueden agregar seguimientos a un reporte en estado " + reporte.getEstado());
        }

        verificarPermisoVoluntarioOAdmin(reporteId, usuarioActual);

        if (request.estadoNuevo() == EstadoReporte.RESCATADO) {
            throw new BusinessRuleException("Para cerrar el rescate como RESCATADO debe utilizar el endpoint especifico de cierre (/cerrar).");
        }

        reporte.setEstado(request.estadoNuevo());

        if (request.urlEvidencia() != null && !request.urlEvidencia().isBlank()) {
            var evidenciaSeg = Evidencia.builder()
                    .reporte(reporte)
                    .urlFoto(request.urlEvidencia().trim())
                    .tipo(TipoEvidencia.SEGUIMIENTO)
                    .fecha(LocalDateTime.now())
                    .descripcion(request.observacion().trim())
                    .build();
            reporte.getEvidencias().add(evidenciaRepository.save(evidenciaSeg));
        }

        Usuario autor = (usuarioActual != null)
                ? usuarioActual
                : usuarioRepository.findByRolAndActivoTrue(RolUsuario.VOLUNTARIO)
                        .stream()
                        .findFirst()
                        .orElse(reporte.getCiudadano());

        registrarSeguimientoInterno(
                reporte,
                autor,
                request.estadoNuevo(),
                request.observacion().trim(),
                request.urlEvidencia()
        );

        return mapToDetalleResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public ReporteDetalleResponse cerrarRescate(Long reporteId, CerrarReporteRequest request, Usuario usuarioActual) {
        var reporte = obtenerEntidadReporte(reporteId);

        if (reporte.getEstado() == EstadoReporte.RESCATADO) {
            throw new BusinessRuleException("Este reporte ya fue cerrado como RESCATADO anteriormente.");
        }

        if (reporte.getEstado() == EstadoReporte.RECHAZADO) {
            throw new BusinessRuleException("No se puede cerrar como RESCATADO un reporte RECHAZADO.");
        }

        verificarPermisoVoluntarioOAdmin(reporteId, usuarioActual);

        long conteoSeguimientosPrevios = seguimientoRepository.countByReporteId(reporteId);
        if (conteoSeguimientosPrevios < 1) {
            throw new BusinessRuleException("No se puede cerrar el rescate sin al menos un seguimiento registrado previamente.");
        }

        if (request.urlFotoCierre() == null || request.urlFotoCierre().isBlank()) {
            throw new BusinessRuleException("Es obligatorio adjuntar la evidencia fotografica de cierre para marcar el caso como RESCATADO.");
        }

        var evidenciaCierre = Evidencia.builder()
                .reporte(reporte)
                .urlFoto(request.urlFotoCierre().trim())
                .tipo(TipoEvidencia.CIERRE)
                .fecha(LocalDateTime.now())
                .descripcion(request.descripcionEvidencia().trim())
                .build();
        reporte.getEvidencias().add(evidenciaRepository.save(evidenciaCierre));

        reporte.setEstado(EstadoReporte.RESCATADO);

        Usuario autor = (usuarioActual != null)
                ? usuarioActual
                : usuarioRepository.findByRolAndActivoTrue(RolUsuario.VOLUNTARIO)
                        .stream()
                        .findFirst()
                        .orElse(reporte.getCiudadano());

        registrarSeguimientoInterno(
                reporte,
                autor,
                EstadoReporte.RESCATADO,
                request.observacionFinal().trim(),
                request.urlFotoCierre().trim()
        );

        for (Asignacion asignacion : reporte.getAsignaciones()) {
            if (asignacion.getEstado() == EstadoAsignacion.ACTIVA) {
                asignacion.setEstado(EstadoAsignacion.COMPLETADA);
                asignacionRepository.save(asignacion);
            }
        }

        return mapToDetalleResponse(reporteRepository.save(reporte));
    }

    @Transactional(readOnly = true)
    public List<MarcadorMapaResponse> obtenerMarcadoresMapa(Especie especie, EstadoReporte estado) {
        return reporteRepository.findWithFilters(especie, estado, null, null, null)
                .stream()
                .filter(r -> r.getEstado() != EstadoReporte.RECHAZADO)
                .map(r -> {
                    var fotoMiniatura = r.getEvidencias().stream()
                            .findFirst()
                            .map(Evidencia::getUrlFoto)
                            .orElse(null);

                    var colorHex = switch (r.getEstado()) {
                        case PENDIENTE -> "#FFA726";
                        case APROBADO -> "#29B6F6";
                        case ASIGNADO -> "#AB47BC";
                        case EN_PROCESO -> "#FFEE58";
                        case RESCATADO -> "#66BB6A";
                        case RECHAZADO -> "#EF5350";
                    };

                    var descCorta = r.getDescripcion().length() > 90
                            ? r.getDescripcion().substring(0, 87) + "..."
                            : r.getDescripcion();

                    return new MarcadorMapaResponse(
                            r.getId(),
                            r.getEspecie(),
                            r.getEstado(),
                            r.getPrioridad(),
                            r.getLatitud(),
                            r.getLongitud(),
                            r.getDireccionReferencia(),
                            descCorta,
                            r.getFechaCreacion(),
                            fotoMiniatura,
                            colorHex
                    );
                })
                .toList();
    }

    private void verificarPermisoVoluntarioOAdmin(Long reporteId, Usuario usuario) {
        if (usuario == null) {
            return;
        }
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR || usuario.getRol() == RolUsuario.VOLUNTARIO) {
            return;
        }
        throw new UnauthorizedOperationException("Rol no autorizado para modificar el seguimiento del rescate.");
    }

    private void registrarSeguimientoInterno(
            Reporte reporte,
            Usuario autor,
            EstadoReporte estadoNuevo,
            String observacion,
            String urlEvidencia
    ) {
        var seguimiento = Seguimiento.builder()
                .reporte(reporte)
                .usuario(autor)
                .fecha(LocalDateTime.now())
                .estadoNuevo(estadoNuevo)
                .observacion(observacion)
                .urlEvidencia(urlEvidencia)
                .build();
        reporte.getSeguimientos().add(seguimientoRepository.save(seguimiento));
    }

    private Reporte obtenerEntidadReporte(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el reporte con ID: " + id));
    }

    private ReporteResumenResponse mapToResumenResponse(Reporte r) {
        var fotoPrincipal = r.getEvidencias().stream()
                .findFirst()
                .map(Evidencia::getUrlFoto)
                .orElse(null);

        return new ReporteResumenResponse(
                r.getId(),
                r.getEspecie(),
                r.getDescripcion(),
                r.getFechaCreacion(),
                r.getEstado(),
                r.getPrioridad(),
                r.getCiudadano().getId(),
                r.getCiudadano().getNombre(),
                r.getLatitud(),
                r.getLongitud(),
                r.getDireccionReferencia(),
                fotoPrincipal
        );
    }

    private ReporteDetalleResponse mapToDetalleResponse(Reporte r) {
        var evidenciasDto = r.getEvidencias().stream()
                .map(e -> new EvidenciaResponse(e.getId(), e.getUrlFoto(), e.getTipo(), e.getFecha(), e.getDescripcion()))
                .toList();

        var asignacionesDto = r.getAsignaciones().stream()
                .map(a -> new AsignacionResponse(
                        a.getId(),
                        a.getVoluntario().getId(),
                        a.getVoluntario().getNombre(),
                        a.getVoluntario().getTelefono(),
                        a.getFechaAsignacion(),
                        a.getEstado()
                ))
                .toList();

        var seguimientosDto = r.getSeguimientos().stream()
                .map(s -> new SeguimientoResponse(
                        s.getId(),
                        s.getUsuario().getId(),
                        s.getUsuario().getNombre(),
                        s.getUsuario().getRol().name(),
                        s.getFecha(),
                        s.getEstadoNuevo(),
                        s.getObservacion(),
                        s.getUrlEvidencia()
                ))
                .toList();

        return new ReporteDetalleResponse(
                r.getId(),
                r.getEspecie(),
                r.getDescripcion(),
                r.getFechaCreacion(),
                r.getEstado(),
                r.getPrioridad(),
                r.getCiudadano().getId(),
                r.getCiudadano().getNombre(),
                r.getLatitud(),
                r.getLongitud(),
                r.getDireccionReferencia(),
                r.getMotivoRechazo(),
                evidenciasDto,
                asignacionesDto,
                seguimientosDto
        );
    }
}