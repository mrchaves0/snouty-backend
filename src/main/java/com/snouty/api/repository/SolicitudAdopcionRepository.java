package com.snouty.api.repository;

import com.snouty.api.entity.SolicitudAdopcion;
import com.snouty.api.entity.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Long> {
    List<SolicitudAdopcion> findAllByOrderByFechaDesc();
    List<SolicitudAdopcion> findByCiudadanoIdOrderByFechaDesc(Long ciudadanoId);
    List<SolicitudAdopcion> findByAnimalIdOrderByFechaDesc(Long animalId);
    List<SolicitudAdopcion> findByAnimalIdAndEstado(Long animalId, EstadoSolicitud estado);
    boolean existsByAnimalIdAndEstado(Long animalId, EstadoSolicitud estado);
    boolean existsByAnimalIdAndCiudadanoIdAndEstado(Long animalId, Long ciudadanoId, EstadoSolicitud estado);
    long countByEstado(EstadoSolicitud estado);
}