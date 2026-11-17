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
        var reporte = Reporte.builder()
                .especie(request.especie())
                .descripcion(request.descripcion().trim())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoReporte.PENDIENTE)
                .prioridad(request.prioridad())
                .ciudadano(ciudadanoAutenticado)
                .latitud(request.latitud())
                .longitud(request.longitud())
                .direccionReferencia(request.direccionReferencia().trim())
                .build();

        var reporteGuardado = reporteRepository.save(reporte);

        if (request.urlFotoInicial() != null && !request.urlFotoInicial().isBlank()) {
            var descripcionFoto = (request.descripcionFotoInicial() != null && !request.descripcionFotoInicial().isBlank())
                    ? request.descripcionFotoInicial().trim()
                    : "Fotografía inicial del reporte registrada por el ciudadano.";

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
        if (admin.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new UnauthorizedOperationException("Solo un ADMINISTRADOR puede aprobar o rechazar reportes.");
        }

        var reporte = obtenerEntidadReporte(reporteId);
        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden validar reportes que estén en estado PENDIENTE. Estado actual: " + reporte.getEstado());
        }

        if (Boolean.TRUE.equals(request.aprobado())) {
            reporte.setEstado(EstadoReporte.APROBADO);
            reporte.setMotivoRechazo(null);

            registrarSeguimientoInterno(
                    reporte,
                    admin,
                    EstadoReporte.APROBADO,
                    "Reporte validado y aprobado por administración para despliegue de rescate.",
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
                    admin,
                    EstadoReporte.RECHAZADO,
                    "Reporte rechazado. Motivo: " + request.motivoRechazo().trim(),
                    null
            );
        }

        return mapToDetalleResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public ReporteDetalleResponse asignarVoluntario(Long reporteId, AsignarVoluntarioRequest request, Usuario admin) {
        if (admin.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new UnauthorizedOperationException("Solo un ADMINISTRADOR puede asignar voluntarios a un reporte.");
        }

        var reporte = obtenerEntidadReporte(reporteId);
        if (reporte.getEstado() != EstadoReporte.APROBADO && reporte.getEstado() != EstadoReporte.ASIGNADO && reporte.getEstado() != EstadoReporte.EN_PROCESO) {
            throw new BusinessRuleException("Solo se pueden asignar voluntarios a reportes en estado APROBADO, ASIGNADO o EN_PROCESO.");
        }

        var voluntario = usuarioRepository.findById(request.voluntarioId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con ID: " + request.voluntarioId()));

        if (voluntario.getRol() != RolUsuario.VOLUNTARIO) {
            throw new BusinessRuleException("El usuario seleccionado no tiene el rol de VOLUNTARIO.");
        }

        if (asignacionRepository.findByReporteIdAndVoluntarioId(reporteId, voluntario.getId()).isPresent()) {
            throw new BusinessRuleException("El voluntario ya se encuentra asignado a este reporte.");
        }

        var asignacion = Asignacion.builder()
                .reporte(reporte)
                .voluntario(voluntario)
                .fechaAsignacion(LocalDateTime.now())
                .estado(EstadoAsignacion.ACTIVA)
                .build();

        reporte.getAsignaciones().add(asignacionRepository.save(asignacion));

        if (reporte.getEstado() == EstadoReporte.APROBADO) {
            reporte.setEstado(EstadoReporte.ASIGNADO);
        }

        registrarSeguimientoInterno(
                reporte,
                admin,
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
            throw new BusinessRuleException("Para cerrar el rescate como RESCATADO debe utilizar el endpoint específico de cierre (/cerrar).");
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

        registrarSeguimientoInterno(
                reporte,
                usuarioActual,
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

        if (reporte.getEstado() == EstadoReporte.PENDIENTE || reporte.getEstado() == EstadoReporte.RECHAZADO) {
            throw new BusinessRuleException("No se puede cerrar como RESCATADO un reporte en estado " + reporte.getEstado());
        }

        verificarPermisoVoluntarioOAdmin(reporteId, usuarioActual);

        long conteoSeguimientosPrevios = seguimientoRepository.countByReporteId(reporteId);
        if (conteoSeguimientosPrevios < 1) {
            throw new BusinessRuleException("No se puede cerrar el rescate sin al menos un seguimiento registrado previamente.");
        }

        if (request.urlFotoCierre() == null || request.urlFotoCierre().isBlank()) {
            throw new BusinessRuleException("Es obligatorio adjuntar la evidencia fotográfica de cierre para marcar el caso como RESCATADO.");
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

        registrarSeguimientoInterno(
                reporte,
                usuarioActual,
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
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            return;
        }
        if (usuario.getRol() == RolUsuario.VOLUNTARIO) {
            boolean estaAsignado = asignacionRepository.existsByReporteIdAndVoluntarioIdAndEstado(
                    reporteId,
                    usuario.getId(),
                    EstadoAsignacion.ACTIVA
            );
            if (!estaAsignado) {
                throw new UnauthorizedOperationException(
                        "El voluntario no tiene una asignación ACTIVA en el reporte #" + reporteId + " para realizar esta acción."
                );
            }
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
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el reporte con ID: " + id));
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