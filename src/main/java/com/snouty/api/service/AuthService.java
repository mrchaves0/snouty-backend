package com.snouty.api.service;

import com.snouty.api.dto.auth.AuthResponse;
import com.snouty.api.dto.auth.LoginRequest;
import com.snouty.api.dto.auth.RegisterRequest;
import com.snouty.api.entity.Usuario;
import com.snouty.api.exception.BusinessRuleException;
import com.snouty.api.repository.UsuarioRepository;
import com.snouty.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo().trim().toLowerCase())) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con el correo: " + request.correo());
        }

        var usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .correo(request.correo().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .telefono(request.telefono().trim())
                .rol(request.rol())
                .activo(true)
                .build();

        var guardado = usuarioRepository.save(usuario);
        var token = jwtService.generateToken(guardado);

        return new AuthResponse(
                token,
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo(),
                guardado.getRol()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var correoNormalizado = request.correo().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correoNormalizado, request.password())
        );

        var usuario = usuarioRepository.findByCorreo(correoNormalizado)
                .orElseThrow(() -> new BusinessRuleException("Usuario no encontrado."));

        var token = jwtService.generateToken(usuario);
        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}