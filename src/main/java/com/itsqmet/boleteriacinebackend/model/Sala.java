package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "salas")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la sala no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotNull(message = "El número de filas no puede estar vacío")
    @Min(value = 1, message = "La sala debe tener al menos 1 fila")
    @Column(nullable = false)
    private Integer filas;

    @NotNull(message = "El número de columnas no puede estar vacío")
    @Min(value = 1, message = "La sala debe tener al menos 1 columna")
    @Column(nullable = false)
    private Integer columnas;

    @NotBlank(message = "El tipo de sala no puede estar vacío")
    @Pattern(regexp = "STANDARD|3D|4D|VIP|IMAX", message = "El tipo de sala debe ser STANDARD, 3D, 4D, VIP o IMAX")
    @Column(nullable = false)
    private String tipoSala;

    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("sala")
    private List<Asiento> asientos;

    @OneToMany(mappedBy = "sala")
    @JsonIgnoreProperties("sala")
    private List<Funcion> funciones;
}
