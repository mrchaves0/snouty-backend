package com.snouty.api.repository;

import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Usuario> findByRolAndActivoTrue(RolUsuario rol);
}