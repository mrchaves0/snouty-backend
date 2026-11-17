package com.snouty.api.service;

import com.snouty.api.dto.estadistica.EstadisticasResponse;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.entity.enums.EstadoReporte;
import com.snouty.api.entity.enums.EstadoSolicitud;
import com.snouty.api.repository.AnimalRepository;
import com.snouty.api.repository.ReporteRepository;
import com.snouty.api.repository.SolicitudAdopcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private final ReporteRepository reporteRepository;
    private final AnimalRepository animalRepository;
    private final SolicitudAdopcionRepository solicitudAdopcionRepository;

    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerEstadisticasGenerales() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoReporte estado : EstadoReporte.values()) {
            porEstado.put(estado.name(), reporteRepository.countByEstado(estado));
        }

        long totalReportes = reporteRepository.count();
        long reportesPerros = reporteRepository.countByEspecie(Especie.PERRO);
        long reportesGatos = reporteRepository.countByEspecie(Especie.GATO);
        long rescatesExitosos = porEstado.getOrDefault(EstadoReporte.RESCATADO.name(), 0L);

        long totalAnimales = animalRepository.count();
        long perrosDisponibles = animalRepository.countByEspecieAndEstadoAdopcion(Especie.PERRO, EstadoAdopcion.DISPONIBLE);
        long gatosDisponibles = animalRepository.countByEspecieAndEstadoAdopcion(Especie.GATO, EstadoAdopcion.DISPONIBLE);
        long animalesAdoptados = animalRepository.countByEstadoAdopcion(EstadoAdopcion.ADOPTADO);

        long solicitudesPendientes = solicitudAdopcionRepository.countByEstado(EstadoSolicitud.PENDIENTE);
        long solicitudesAprobadas = solicitudAdopcionRepository.countByEstado(EstadoSolicitud.APROBADA);

        return new EstadisticasResponse(
                totalReportes,
                reportesPerros,
                reportesGatos,
                rescatesExitosos,
                porEstado,
                totalAnimales,
                perrosDisponibles,
                gatosDisponibles,
                animalesAdoptados,
                solicitudesPendientes,
                solicitudesAprobadas
        );
    }
}
