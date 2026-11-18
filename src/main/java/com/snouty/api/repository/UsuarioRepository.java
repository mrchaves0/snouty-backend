package com.snouty.api.repository;

import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByRolAndActivoTrue(RolUsuario rol);

    List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(RolUsuario rol);

    List<Usuario> findByRolOrderByNombreAsc(RolUsuario rol);
}