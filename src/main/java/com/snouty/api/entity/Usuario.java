package com.snouty.api.entity;

import com.snouty.api.entity.enums.RolUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String telefono;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (rol == null) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_CIUDADANO"),
                    new SimpleGrantedAuthority("CIUDADANO")
            );
        }
        String roleName = rol.name().trim().toUpperCase();
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + roleName),
                new SimpleGrantedAuthority(roleName)
        );
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(activo);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activo);
    }
}