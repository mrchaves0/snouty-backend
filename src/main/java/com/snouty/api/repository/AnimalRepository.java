package com.snouty.api.repository;

import com.snouty.api.entity.Animal;
import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {

    boolean existsByReporteOrigenId(Long reporteOrigenId);

    Optional<Animal> findByReporteOrigenId(Long reporteOrigenId);

    List<Animal> findByEspecieOrderByIdDesc(Especie especie);

    List<Animal> findByEstadoAdopcionOrderByIdDesc(EstadoAdopcion estadoAdopcion);

    List<Animal> findByEspecieAndEstadoAdopcionOrderByIdDesc(Especie especie, EstadoAdopcion estadoAdopcion);

    List<Animal> findAllByOrderByIdDesc();

    long countByEspecieAndEstadoAdopcion(Especie especie, EstadoAdopcion estadoAdopcion);

    long countByEstadoAdopcion(EstadoAdopcion estadoAdopcion);
}