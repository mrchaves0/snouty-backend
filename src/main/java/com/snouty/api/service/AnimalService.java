package com.snouty.api.service;

import com.snouty.api.dto.animal.*;
import com.snouty.api.entity.Animal;
import com.snouty.api.entity.Reporte;
import com.snouty.api.entity.RevisionVeterinaria;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.*;
import com.snouty.api.exception.BusinessRuleException;
import com.snouty.api.exception.ResourceNotFoundException;
import com.snouty.api.exception.UnauthorizedOperationException;
import com.snouty.api.repository.AnimalRepository;
import com.snouty.api.repository.ReporteRepository;
import com.snouty.api.repository.RevisionVeterinariaRepository;
import com.snouty.api.repository.UsuarioRepository;
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
    private final RevisionVeterinariaRepository revisionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AnimalResponse registrarAnimal(AnimalCreateRequest request) {
        var reporte = reporteRepository.findById(request.reporteOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el reporte de origen con ID: " + request.reporteOrigenId()));

        if (reporte.getEstado() != EstadoReporte.RESCATADO) {
            throw new BusinessRuleException("Solo se puede registrar en catalogo un animal cuyo reporte este en estado RESCATADO.");
        }

        if (animalRepository.existsByReporteOrigenId(reporte.getId())) {
            throw new BusinessRuleException("Ya existe una ficha de animal registrada para el reporte #" + reporte.getId());
        }

        var animal = Animal.builder()
                .reporteOrigen(reporte)
                .especie(request.especie())
                .nombreProvisional(request.nombreProvisional().trim())
                .edadAproximada(request.edadAproximada().trim())
                .sexo(request.sexo())
                .tamano(request.tamano())
                .descripcion(request.descripcion().trim())
                .urlFoto(request.urlFoto().trim())
                .estadoAdopcion(EstadoAdopcion.EN_RECUPERACION)
                .build();

        return mapToResponse(animalRepository.save(animal));
    }

    @Transactional
    public List<AnimalResponse> listarAnimales(Especie especie, EstadoAdopcion estado) {
        List<Reporte> reportesRescatados = reporteRepository.findAll().stream()
                .filter(r -> r.getEstado() == EstadoReporte.RESCATADO)
                .toList();

        for (Reporte rep : reportesRescatados) {
            if (!animalRepository.existsByReporteOrigenId(rep.getId())) {
                String foto = rep.getEvidencias().isEmpty()
                        ? "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800"
                        : rep.getEvidencias().get(rep.getEvidencias().size() - 1).getUrlFoto();

                String etiqueta = (rep.getEspecie() == Especie.GATO) ? "Michi" : "Peludito";
                var nuevo = Animal.builder()
                        .reporteOrigen(rep)
                        .especie(rep.getEspecie())
                        .nombreProvisional(etiqueta + " Rescatado #" + rep.getId())
                        .edadAproximada("Pendiente valoracion")
                        .sexo(SexoAnimal.DESCONOCIDO)
                        .tamano(TamanoAnimal.MEDIANO)
                        .descripcion("Ingresado al refugio tras rescate en " + rep.getDireccionReferencia() + ". " + rep.getDescripcion())
                        .urlFoto(foto)
                        .estadoAdopcion(EstadoAdopcion.EN_RECUPERACION)
                        .build();
                animalRepository.save(nuevo);
            }
        }

        return animalRepository.findAllByOrderByIdDesc().stream()
                .filter(a -> especie == null || a.getEspecie() == especie)
                .filter(a -> estado == null || a.getEstadoAdopcion() == estado)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse obtenerAnimalPorId(Long id) {
        var animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el animal con ID: " + id));
        return mapToResponse(animal);
    }

    @Transactional
    public AnimalResponse cambiarEstadoAdopcion(Long id, EstadoAdopcion nuevoEstado) {
        var animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el animal con ID: " + id));

        if (nuevoEstado == EstadoAdopcion.DISPONIBLE) {
            boolean tieneAval = revisionRepository.existsByAnimalIdAndAptoAdopcionTrue(animal.getId());
            if (!tieneAval) {
                throw new BusinessRuleException("No se puede publicar como DISPONIBLE sin aval veterinario.");
            }
        }

        animal.setEstadoAdopcion(nuevoEstado);
        return mapToResponse(animalRepository.save(animal));
    }

    @Transactional(readOnly = true)
    public List<RevisionVeterinariaResponse> listarRevisionesPorAnimal(Long animalId) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el animal con ID: " + animalId));

        return animal.getRevisiones() == null
                ? List.of()
                : animal.getRevisiones().stream()
                .map(r -> new RevisionVeterinariaResponse(
                        r.getId(),
                        animal.getId(),
                        animal.getNombreProvisional(),
                        r.getVeterinario().getId(),
                        r.getVeterinario().getNombre(),
                        r.getFecha(),
                        r.getDiagnostico(),
                        r.getObservaciones(),
                        r.getAptoAdopcion()
                ))
                .toList();
    }

    @Transactional
    public RevisionVeterinariaResponse registrarRevisionVeterinaria(
            Long animalId,
            RevisionVeterinariaCreateRequest request,
            Usuario veterinario
    ) {
        if (veterinario != null && veterinario.getRol() != RolUsuario.VETERINARIO && veterinario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new UnauthorizedOperationException("Solo un VETERINARIO puede emitir valoraciones clinicas.");
        }

        Usuario vetAutor = (veterinario != null)
                ? veterinario
                : usuarioRepository.findByRolAndActivoTrue(RolUsuario.VETERINARIO)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No se encontro usuario veterinario."));

        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el animal con ID: " + animalId));

        var revision = RevisionVeterinaria.builder()
                .animal(animal)
                .veterinario(vetAutor)
                .fecha(LocalDateTime.now())
                .diagnostico(request.diagnostico().trim())
                .observaciones(request.observaciones().trim())
                .aptoAdopcion(Boolean.TRUE.equals(request.aptoAdopcion()))
                .build();

        var guardada = revisionRepository.save(revision);

        if (Boolean.TRUE.equals(request.aptoAdopcion()) && Boolean.TRUE.equals(request.publicarDisponibleInmediatamente())) {
            animal.setEstadoAdopcion(EstadoAdopcion.DISPONIBLE);
            animalRepository.save(animal);
        }

        return new RevisionVeterinariaResponse(
                guardada.getId(),
                animal.getId(),
                animal.getNombreProvisional(),
                vetAutor.getId(),
                vetAutor.getNombre(),
                guardada.getFecha(),
                guardada.getDiagnostico(),
                guardada.getObservaciones(),
                guardada.getAptoAdopcion()
        );
    }

    private AnimalResponse mapToResponse(Animal a) {
        boolean tieneAval = revisionRepository.existsByAnimalIdAndAptoAdopcionTrue(a.getId());

        var revisionesDto = a.getRevisiones() == null
                ? List.<RevisionVeterinariaResponse>of()
                : a.getRevisiones().stream()
                .map(r -> new RevisionVeterinariaResponse(
                        r.getId(),
                        a.getId(),
                        a.getNombreProvisional(),
                        r.getVeterinario().getId(),
                        r.getVeterinario().getNombre(),
                        r.getFecha(),
                        r.getDiagnostico(),
                        r.getObservaciones(),
                        r.getAptoAdopcion()
                ))
                .toList();

        return new AnimalResponse(
                a.getId(),
                a.getReporteOrigen() != null ? a.getReporteOrigen().getId() : 0L,
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
}