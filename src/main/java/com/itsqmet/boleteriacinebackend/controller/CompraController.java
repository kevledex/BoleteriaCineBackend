package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.dto.EntradaUsuarioDTO;
import com.itsqmet.boleteriacinebackend.model.Compra;
import com.itsqmet.boleteriacinebackend.service.CompraService;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

  @Autowired
  private CompraService compraService;

  @PostMapping("/registrar")
  public ResponseEntity<?> registrar(
    @RequestBody CompraService.CompraSolicitud solicitud,
    Authentication auth
  ) {
    try {
      Compra compra = compraService.registrarCompra(solicitud, auth.getName());
      return ResponseEntity.status(HttpStatus.CREATED).body(
        Map.of(
          "id",
          compra.getId(),
          "total",
          compra.getTotal(),
          "estado",
          compra.getEstado()
        )
      );
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (DataIntegrityViolationException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(
        Map.of("error", "Uno de los asientos acaba de ser vendido")
      );
    }
  }

  @GetMapping
  public ResponseEntity<List<Compra>> obtenerTodo() {
    return ResponseEntity.ok(compraService.obtenerTodo());
  }

  @GetMapping("/mias")
  public ResponseEntity<List<EntradaUsuarioDTO>> obtenerMisEntradas(
    Authentication auth
  ) {
    return ResponseEntity.ok(
      compraService.obtenerEntradasPorCorreo(auth.getName())
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
    return compraService
      .buscarPorId(id)
      .map(compra -> ResponseEntity.ok((Object) compra))
      .orElse(
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          Map.of("error", "Compra con id " + id + " no encontrada")
        )
      );
  }

  @GetMapping("/usuario/{usuarioId}")
  public ResponseEntity<List<Compra>> obtenerPorUsuario(
    @PathVariable Long usuarioId
  ) {
    return ResponseEntity.ok(compraService.obtenerPorUsuario(usuarioId));
  }

  @PostMapping
  public ResponseEntity<?> crear(@RequestBody Compra compra) {
    if (compra.getUsuario() == null || compra.getUsuario().getId() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        Map.of("error", "Debe indicar el usuario de la compra")
      );
    }
    if (compra.getMetodoPago() == null || compra.getMetodoPago().isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        Map.of("error", "Debe indicar el método de pago")
      );
    }

    try {
      Optional<String> error = compraService.crearCompra(compra);
      if (error.isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
          Map.of("error", error.get())
        );
      }
      return ResponseEntity.status(HttpStatus.CREATED).body(compra);
    } catch (ConstraintViolationException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        Map.of("error", e.getMessage())
      );
    }
  }

  @PutMapping("/{id}/confirmar")
  public ResponseEntity<?> confirmarPago(@PathVariable Long id) {
    return compraService
      .confirmarPago(id)
      .map(compra -> ResponseEntity.ok((Object) compra))
      .orElse(
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          Map.of("error", "Compra con id " + id + " no encontrada")
        )
      );
  }

  @PutMapping("/{id}/cancelar")
  public ResponseEntity<?> cancelar(@PathVariable Long id) {
    return compraService
      .cancelar(id)
      .map(compra -> ResponseEntity.ok((Object) compra))
      .orElse(
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          Map.of("error", "Compra con id " + id + " no encontrada")
        )
      );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> eliminar(@PathVariable Long id) {
    if (compraService.eliminar(id)) {
      return ResponseEntity.ok(
        Map.of("mensaje", "Compra eliminada correctamente")
      );
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      Map.of("error", "Compra con id " + id + " no encontrada")
    );
  }
}
