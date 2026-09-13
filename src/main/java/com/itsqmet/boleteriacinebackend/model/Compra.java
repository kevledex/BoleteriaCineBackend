package com.itsqmet.boleteriacinebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties("compras")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCompra;

    @NotNull(message = "El total no puede estar vacío")
    @DecimalMin(value = "0.0", message = "El total no puede ser negativo")
    @Column(nullable = false)
    private Double total;

    @NotBlank(message = "El estado de la compra no puede estar vacío")
    @Pattern(regexp = "PENDIENTE|PAGADA|CANCELADA", message = "El estado debe ser PENDIENTE, PAGADA o CANCELADA")
    @Column(nullable = false)
    private String estado;

    @NotBlank(message = "El método de pago no puede estar vacío")
    @Pattern(regexp = "TARJETA|EFECTIVO|PAYPAL", message = "El método de pago debe ser TARJETA, EFECTIVO o PAYPAL")
    @Column(nullable = false)
    private String metodoPago;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("compra")
    private List<DetalleBoleto> detallesBoleto;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("compra")
    private List<DetalleSnack> detallesSnack;
}
