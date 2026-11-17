package com.snouty.api.entity;

import com.snouty.api.entity.enums.Especie;
import com.snouty.api.entity.enums.EstadoAdopcion;
import com.snouty.api.entity.enums.SexoAnimal;
import com.snouty.api.entity.enums.TamanoAnimal;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_origen_id", nullable = false, unique = true)
    private Reporte reporteOrigen;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Especie especie;

    @NotBlank
    @Column(name = "nombre_provisional", nullable = false, length = 100)
    private String nombreProvisional;

    @NotBlank
    @Column(name = "edad_aproximada", nullable = false, length = 50)
    private String edadAproximada;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SexoAnimal sexo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TamanoAnimal tamano;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank
    @Column(name = "url_foto", nullable = false, length = 500)
    private String urlFoto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "estado_adopcion", nullable = false, length = 35)
    private EstadoAdopcion estadoAdopcion = EstadoAdopcion.EN_RECUPERACION;

    @Builder.Default
    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    private List<RevisionVeterinaria> revisiones = new ArrayList<>();
}
