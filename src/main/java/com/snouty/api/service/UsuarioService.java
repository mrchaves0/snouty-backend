package com.snouty.api.service;

import com.snouty.api.dto.usuario.VoluntarioResponse;
import com.snouty.api.entity.Usuario;
import com.snouty.api.entity.enums.RolUsuario;
import com.snouty.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<VoluntarioResponse> listarVoluntariosActivos() {
        var voluntariosActivos = usuarioRepository.findByRolAndActivoTrueOrderByNombreAsc(RolUsuario.VOLUNTARIO);

        if (voluntariosActivos.isEmpty()) {
            voluntariosActivos = usuarioRepository.findByRolOrderByNombreAsc(RolUsuario.VOLUNTARIO);
        }

        return voluntariosActivos.stream()
                .map(this::mapToVoluntarioResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VoluntarioResponse> listarUsuariosPorRol(RolUsuario rol) {
        if (rol == null) {
            return usuarioRepository.findAll().stream()
                    .map(this::mapToVoluntarioResponse)
                    .toList();
        }
        return usuarioRepository.findByRolOrderByNombreAsc(rol).stream()
                .map(this::mapToVoluntarioResponse)
                .toList();
    }

    private VoluntarioResponse mapToVoluntarioResponse(Usuario u) {
        return new VoluntarioResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getTelefono(),
                u.getRol(),
                u.getActivo()
        );
    }
}