package com.itsqmet.boleteriacinebackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record EntradaUsuarioDTO(
  Long id,
  String codigo,
  LocalDateTime fechaCompra,
  Double total,
  String estado,
  String pelicula,
  LocalDate fechaFuncion,
  LocalTime hora,
  String formato,
  String idioma,
  String sala,
  List<String> asientos,
  List<SnackDTO> snacks
) {
  public record SnackDTO(
    String nombre,
    Integer cantidad,
    Double precioUnitario,
    Double subtotal
  ) {}
}
