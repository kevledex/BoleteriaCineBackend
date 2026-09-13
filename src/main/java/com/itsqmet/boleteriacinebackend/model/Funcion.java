package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "funciones")
public class Funcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El identificador de la película no puede estar vacío")
    @Column(nullable = false)
    private String peliculaId;

    @NotBlank(message = "El título de la película no puede estar vacío")
    @Column(nullable = false)
    private String tituloPelicula;

    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    @JsonIgnoreProperties({"funciones", "asientos"})
    private Sala sala;

    @NotNull(message = "La fecha de la función no puede estar vacía")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la función no puede estar vacía")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El formato de la función no puede estar vacío")
    @Pattern(regexp = "2D|3D", message = "El formato debe ser 2D o 3D")
    @Column(nullable = false)
    private String formato;

    @NotBlank(message = "El idioma de la función no puede estar vacío")
    @Pattern(regexp = "SUBTITULADA|DOBLADA", message = "El idioma debe ser SUBTITULADA o DOBLADA")
    @Column(nullable = false)
    private String idioma;

    @NotNull(message = "El precio base no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    @Column(nullable = false)
    private Double precioBase;

    @OneToMany(mappedBy = "funcion")
    @JsonIgnoreProperties("funcion")
    private List<DetalleBoleto> detallesBoleto;
}
