package com.snouty.api.entity;

import com.snouty.api.entity.enums.EstadoSolicitud;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_adopcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAdopcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Usuario ciudadano;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 25)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "observacion_admin", columnDefinition = "TEXT")
    private String observacionAdmin;
}