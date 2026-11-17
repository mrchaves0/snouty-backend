package com.snouty.api.service;

import com.snouty.api.dto.animal.*;
import com.snouty.api.entity.Animal;
import com.snouty.api.entity.RevisionVeterinaria;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.RolUsuario;
import com.snouty.api.exception.BusinessRuleException;
import com.snouty.api.exception.ResourceNotFoundException;
import com.snouty.api.exception.UnauthorizedOperationException;
import com.snouty.api.repository.AnimalRepository;
import com.snouty.api.repository.ReporteRepository;
import com.snouty.api.repository.RevisionVeterinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final ReporteRepository reporteRepository;
    private final RevisionVeterinariaRepository revisionVeterinariaRepository;

    @Transactional
    public AnimalResponse registrarAnimal(AnimalCreateRequest request) {
        var reporteOrigen = reporteRepository.findById(request.reporteOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el reporte de origen con ID: " + request.reporteOrigenId()));

        if (reporteOrigen.getEstado() != EstadoReporte.RESCATADO) {
            throw new BusinessRuleException(
                    "Solo se puede crear la ficha de un animal a partir de un reporte en estado RESCATADO. Estado actual del reporte: "
                            + reporteOrigen.getEstado()
            );
        }

        if (animalRepository.existsByReporteOrigenId(reporteOrigen.getId())) {
            throw new BusinessRuleException("El reporte #" + reporteOrigen.getId() + " ya tiene una ficha de animal vinculada.");
        }

        if (request.especie() != null && request.especie() != reporteOrigen.getEspecie()) {
            throw new BusinessRuleException(
                    "La especie del animal (" + request.especie() + ") no coincide con la especie del reporte de origen ("
                            + reporteOrigen.getEspecie() + ")."
            );
        }

        var animal = Animal.builder()
                .reporteOrigen(reporteOrigen)
                .especie(reporteOrigen.getEspecie())
                .nombreProvisional(request.nombreProvisional().trim())
                .edadAproximada(request.edadAproximada().trim())
                .sexo(request.sexo())
                .tamano(request.tamano())
                .descripcion(request.descripcion().trim())
                .urlFoto(request.urlFoto().trim())
                .estadoAdopcion(EstadoAdopcion.EN_RECUPERACION)
                .build();

        return mapToAnimalResponse(animalRepository.save(animal));
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> listarAnimales(Especie especie, EstadoAdopcion estadoAdopcion) {
        return animalRepository.findWithFilters(especie, estadoAdopcion)
                .stream()
                .map(this::mapToAnimalResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse obtenerAnimalPorId(Long animalId) {
        var animal = obtenerEntidadAnimal(animalId);
        return mapToAnimalResponse(animal);
    }

    @Transactional
    public RevisionVeterinariaResponse registrarRevisionVeterinaria(
            Long animalId,
            RevisionVeterinariaCreateRequest request,
            Usuario veterinario
    ) {
        if (veterinario.getRol() != RolUsuario.VETERINARIO) {
            throw new UnauthorizedOperationException("Exclusivamente un usuario con rol VETERINARIO puede registrar revisiones clínicas.");
        }

        var animal = obtenerEntidadAnimal(animalId);

        var revision = RevisionVeterinaria.builder()
                .animal(animal)
                .veterinario(veterinario)
                .fecha(LocalDateTime.now())
                .diagnostico(request.diagnostico().trim())
                .observaciones(request.observaciones().trim())
                .aptoAdopcion(request.aptoAdopcion())
                .build();

        var revisionGuardada = revisionVeterinariaRepository.save(revision);
        animal.getRevisiones().add(revisionGuardada);

        if (Boolean.TRUE.equals(request.aptoAdopcion())
                && Boolean.TRUE.equals(request.publicarDisponibleInmediatamente())
                && animal.getEstadoAdopcion() == EstadoAdopcion.EN_RECUPERACION) {
            animal.setEstadoAdopcion(EstadoAdopcion.DISPONIBLE);
            animalRepository.save(animal);
        }

        return mapToRevisionResponse(revisionGuardada);
    }

    @Transactional(readOnly = true)
    public List<RevisionVeterinariaResponse> listarRevisionesPorAnimal(Long animalId) {
        obtenerEntidadAnimal(animalId);
        return revisionVeterinariaRepository.findByAnimalIdOrderByFechaDesc(animalId)
                .stream()
                .map(this::mapToRevisionResponse)
                .toList();
    }

    @Transactional
    public AnimalResponse cambiarEstadoAdopcion(Long animalId, EstadoAdopcion nuevoEstado) {
        var animal = obtenerEntidadAnimal(animalId);

        if (nuevoEstado == EstadoAdopcion.DISPONIBLE) {
            boolean tieneAval = revisionVeterinariaRepository.existsByAnimalIdAndAptoAdopcionTrue(animalId);
            if (!tieneAval) {
                throw new BusinessRuleException(
                        "El animal '" + animal.getNombreProvisional()
                                + "' no puede pasar a estado DISPONIBLE porque no cuenta con una revisión veterinaria con aptoAdopcion = true."
                );
            }
        }

        animal.setEstadoAdopcion(nuevoEstado);
        return mapToAnimalResponse(animalRepository.save(animal));
    }

    private Animal obtenerEntidadAnimal(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el animal con ID: " + id));
    }

    private AnimalResponse mapToAnimalResponse(Animal a) {
        var revisionesDto = a.getRevisiones().stream()
                .map(this::mapToRevisionResponse)
                .toList();

        boolean tieneAval = a.getRevisiones().stream()
                .anyMatch(r -> Boolean.TRUE.equals(r.getAptoAdopcion()));

        return new AnimalResponse(
                a.getId(),
                a.getReporteOrigen().getId(),
                a.getEspecie(),
                a.getNombreProvisional(),
                a.getEdadAproximada(),
                a.getSexo(),
                a.getTamano(),
                a.getDescripcion(),
                a.getUrlFoto(),
                a.getEstadoAdopcion(),
                tieneAval,
                revisionesDto
        );
    }

    private RevisionVeterinariaResponse mapToRevisionResponse(RevisionVeterinaria r) {
        return new RevisionVeterinariaResponse(
                r.getId(),
                r.getAnimal().getId(),
                r.getAnimal().getNombreProvisional(),
                r.getVeterinario().getId(),
                r.getVeterinario().getNombre(),
                r.getFecha(),
                r.getDiagnostico(),
                r.getObservaciones(),
                r.getAptoAdopcion()
        );
    }
}