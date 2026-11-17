package com.snouty.api.service;

import com.snouty.api.dto.adopcion.ResolverSolicitudAdopcionRequest;
import com.snouty.api.dto.adopcion.SolicitudAdopcionCreateRequest;
import com.snouty.api.dto.adopcion.SolicitudAdopcionResponse;
import com.snouty.api.entity.SolicitudAdopcion;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.entity.enums.EstadoSolicitud;
import com.snouty.api.entity.enums.RolUsuario;
import com.snouty.api.exception.BusinessRuleException;
import com.snouty.api.exception.ResourceNotFoundException;
import com.snouty.api.exception.UnauthorizedOperationException;
import com.snouty.api.repository.AnimalRepository;
import com.snouty.api.repository.RevisionVeterinariaRepository;
import com.snouty.api.repository.SolicitudAdopcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdopcionService {

    private final SolicitudAdopcionRepository solicitudRepository;
    private final AnimalRepository animalRepository;
    private final RevisionVeterinariaRepository revisionVeterinariaRepository;

    @Transactional
    public SolicitudAdopcionResponse crearSolicitud(SolicitudAdopcionCreateRequest request, Usuario solicitante) {
        var animal = animalRepository.findById(request.animalId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el animal con ID: " + request.animalId()));

        if (animal.getEstadoAdopcion() == EstadoAdopcion.ADOPTADO) {
            throw new BusinessRuleException("El animal ya ha sido adoptado.");
        }

        if (animal.getEstadoAdopcion() != EstadoAdopcion.DISPONIBLE && animal.getEstadoAdopcion() != EstadoAdopcion.EN_PROCESO_ADOPCION) {
            throw new BusinessRuleException("El animal no se encuentra disponible para recibir solicitudes de adopción en este momento.");
        }

        boolean tieneAvalVeterinario = revisionVeterinariaRepository.existsByAnimalIdAndAptoAdopcionTrue(animal.getId());
        if (!tieneAvalVeterinario) {
            throw new BusinessRuleException("El animal aún no cuenta con certificado veterinario de aptitud para adopción.");
        }

        if (solicitudRepository.existsByAnimalIdAndCiudadanoIdAndEstado(animal.getId(), solicitante.getId(), EstadoSolicitud.PENDIENTE)) {
            throw new BusinessRuleException("Ya tienes una solicitud de adopción PENDIENTE para este animal.");
        }

        var solicitud = SolicitudAdopcion.builder()
                .animal(animal)
                .ciudadano(solicitante)
                .fecha(LocalDateTime.now())
                .motivo(request.motivo().trim())
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        if (animal.getEstadoAdopcion() == EstadoAdopcion.DISPONIBLE) {
            animal.setEstadoAdopcion(EstadoAdopcion.EN_PROCESO_ADOPCION);
            animalRepository.save(animal);
        }

        return mapToResponse(solicitudRepository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudAdopcionResponse> listarSolicitudes(Usuario usuarioActual) {
        if (usuarioActual.getRol() == RolUsuario.ADMINISTRADOR || usuarioActual.getRol() == RolUsuario.VOLUNTARIO) {
            return solicitudRepository.findAllByOrderByFechaDesc()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return solicitudRepository.findByCiudadanoIdOrderByFechaDesc(usuarioActual.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SolicitudAdopcionResponse resolverSolicitud(
            Long solicitudId,
            ResolverSolicitudAdopcionRequest request,
            Usuario admin
    ) {
        if (admin.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new UnauthorizedOperationException("Solo un ADMINISTRADOR puede aprobar o rechazar solicitudes de adopción.");
        }

        if (request.estado() == EstadoSolicitud.PENDIENTE) {
            throw new BusinessRuleException("Debe resolver la solicitud hacia el estado APROBADA o RECHAZADA.");
        }

        var solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la solicitud de adopción con ID: " + solicitudId));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessRuleException("Esta solicitud ya fue resuelta previamente con estado: " + solicitud.getEstado());
        }

        var animal = solicitud.getAnimal();

        if (request.estado() == EstadoSolicitud.APROBADA) {
            if (animal.getEstadoAdopcion() == EstadoAdopcion.ADOPTADO
                    || solicitudRepository.existsByAnimalIdAndEstado(animal.getId(), EstadoSolicitud.APROBADA)) {
                throw new BusinessRuleException("No se puede aprobar la solicitud porque el animal ya fue asignado en otra adopción.");
            }

            solicitud.setEstado(EstadoSolicitud.APROBADA);
            solicitud.setObservacionAdmin(request.observacionAdmin().trim());
            solicitudRepository.save(solicitud);

            animal.setEstadoAdopcion(EstadoAdopcion.ADOPTADO);
            animalRepository.save(animal);

            var otrasPendientes = solicitudRepository.findByAnimalIdAndEstado(animal.getId(), EstadoSolicitud.PENDIENTE);
            for (SolicitudAdopcion otra : otrasPendientes) {
                if (!otra.getId().equals(solicitud.getId())) {
                    otra.setEstado(EstadoSolicitud.RECHAZADA);
                    otra.setObservacionAdmin("Rechazada automáticamente debido a que se aprobó otra solicitud de adopción para este ejemplar.");
                    solicitudRepository.save(otra);
                }
            }
        } else {
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            solicitud.setObservacionAdmin(request.observacionAdmin().trim());
            solicitudRepository.save(solicitud);

            boolean quedanPendientes = solicitudRepository.existsByAnimalIdAndEstado(animal.getId(), EstadoSolicitud.PENDIENTE);
            boolean tieneAprobada = solicitudRepository.existsByAnimalIdAndEstado(animal.getId(), EstadoSolicitud.APROBADA);
            if (!quedanPendientes && !tieneAprobada && animal.getEstadoAdopcion() == EstadoAdopcion.EN_PROCESO_ADOPCION) {
                animal.setEstadoAdopcion(EstadoAdopcion.DISPONIBLE);
                animalRepository.save(animal);
            }
        }

        return mapToResponse(solicitud);
    }

    private SolicitudAdopcionResponse mapToResponse(SolicitudAdopcion s) {
        return new SolicitudAdopcionResponse(
                s.getId(),
                s.getAnimal().getId(),
                s.getAnimal().getNombreProvisional(),
                s.getAnimal().getEspecie(),
                s.getAnimal().getUrlFoto(),
                s.getCiudadano().getId(),
                s.getCiudadano().getNombre(),
                s.getCiudadano().getCorreo(),
                s.getCiudadano().getTelefono(),
                s.getFecha(),
                s.getMotivo(),
                s.getEstado(),
                s.getObservacionAdmin()
        );
    }
}