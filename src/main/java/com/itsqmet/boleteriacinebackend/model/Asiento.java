package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "asientos", uniqueConstraints = @UniqueConstraint(columnNames = {"sala_id", "fila", "numero"}))
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    @JsonIgnoreProperties("asientos")
    private Sala sala;

    @NotBlank(message = "La fila no puede estar vacía")
    @Size(max = 5, message = "La fila no puede superar los 5 caracteres")
    @Column(nullable = false)
    private String fila;

    @NotNull(message = "El número de asiento no puede estar vacío")
    @Min(value = 1, message = "El número de asiento debe ser mayor a 0")
    @Column(nullable = false)
    private Integer numero;

    @NotBlank(message = "El tipo de asiento no puede estar vacío")
    @Pattern(regexp = "NORMAL|VIP|DISCAPACITADO", message = "El tipo de asiento debe ser NORMAL, VIP o DISCAPACITADO")
    @Column(nullable = false)
    private String tipoAsiento;
}
