package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "detalles_boleto", uniqueConstraints = @UniqueConstraint(columnNames = {"funcion_id", "asiento_id"}))
public class DetalleBoleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonIgnoreProperties({"detallesBoleto", "detallesSnack"})
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "funcion_id", nullable = false)
    @JsonIgnoreProperties("detallesBoleto")
    private Funcion funcion;

    @ManyToOne
    @JoinColumn(name = "asiento_id", nullable = false)
    @JsonIgnoreProperties("sala")
    private Asiento asiento;

    @NotNull(message = "El precio del boleto no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio del boleto debe ser mayor a 0")
    @Column(nullable = false)
    private Double precio;
}
