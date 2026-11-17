package com.snouty.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revisiones_veterinarias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionVeterinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @Builder.Default
    @Column(name = "apto_adopcion", nullable = false)
    private Boolean aptoAdopcion = false;
}
