package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "detalles_snack")
public class DetalleSnack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonIgnoreProperties({"detallesBoleto", "detallesSnack"})
    private Compra compra;

    @NotBlank(message = "El identificador del snack no puede estar vacío")
    @Column(nullable = false)
    private String snackId;

    @NotBlank(message = "El nombre del snack no puede estar vacío")
    @Column(nullable = false)
    private String nombreSnack;

    @NotNull(message = "El precio unitario no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    @Column(nullable = false)
    private Double precioUnitario;

    @NotNull(message = "La cantidad no puede estar vacía")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El subtotal no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = false, message = "El subtotal debe ser mayor a 0")
    @Column(nullable = false)
    private Double subtotal;
}
