package com.snouty.api.repository;

import com.snouty.api.entity.RevisionVeterinaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionVeterinariaRepository extends JpaRepository<RevisionVeterinaria, Long> {
    List<RevisionVeterinaria> findByAnimalIdOrderByFechaDesc(Long animalId);
    boolean existsByAnimalIdAndAptoAdopcionTrue(Long animalId);
}