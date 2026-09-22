package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.model.Asiento;
import com.itsqmet.boleteriacinebackend.model.Funcion;
import com.itsqmet.boleteriacinebackend.service.AsientoBloqueoService;
import com.itsqmet.boleteriacinebackend.service.AsientoService;
import com.itsqmet.boleteriacinebackend.service.DetalleBoletoService;
import com.itsqmet.boleteriacinebackend.service.FuncionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/funciones")
public class FuncionController {

    @Autowired
    private FuncionService funcionService;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private DetalleBoletoService detalleBoletoService;

    @Autowired
    private AsientoBloqueoService asientoBloqueoService;

    // Forma exacta que espera AsientosService del frontend: { id, estado, precio }[]
    private record AsientoDisponibilidad(String id, Long asientoId, String estado, Double precio, String clienteId) {
    }

    @GetMapping
    public ResponseEntity<List<Funcion>> obtenerTodo(
            @RequestParam(required = false) Long peliculaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        if (peliculaId != null && fecha != null) {
            return ResponseEntity.ok(funcionService.obtenerPorPeliculaYFecha(peliculaId, fecha));
        }
        if (peliculaId != null) {
            return ResponseEntity.ok(funcionService.obtenerPorPelicula(peliculaId));
        }
        return ResponseEntity.ok(funcionService.obtenerTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return funcionService.buscarPorId(id)
                .map(funcion -> ResponseEntity.ok((Object) funcion))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Función con id " + id + " no encontrada")));
    }

    // GET /api/funciones/{id}/asientos -> usado por AsientosService.obtenerMapaAsientos en el frontend
    @GetMapping("/{id}/asientos")
    public ResponseEntity<?> obtenerMapaAsientos(@PathVariable Long id) {
        return funcionService.buscarPorId(id)
                .map(funcion -> {
                    List<Asiento> asientosSala = asientoService.obtenerPorSala(funcion.getSala().getId());
                    List<Long> ocupados = detalleBoletoService.obtenerAsientosOcupados(id);
                    java.util.Map<String, String> reservados = asientoBloqueoService.obtenerReservados(id);

                    List<AsientoDisponibilidad> mapa = asientosSala.stream()
                            .map(asiento -> {
                                String codigo = asiento.getFila() + asiento.getNumero();
                                String estado = ocupados.contains(asiento.getId())
                                        ? "OCUPADO"
                                        : reservados.containsKey(codigo) ? "RESERVADO" : "LIBRE";

                                return new AsientoDisponibilidad(
                                        codigo,
                                        asiento.getId(),
                                        estado,
                                        funcion.getPrecioBase(),
                                        reservados.get(codigo));
                            })
                            .collect(Collectors.toList());

                    return ResponseEntity.ok((Object) mapa);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Función con id " + id + " no encontrada")));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Funcion funcion, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Funcion nueva = funcionService.crearFuncion(funcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Funcion funcion, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        return funcionService.actualizar(id, funcion)
                .map(actualizada -> ResponseEntity.ok((Object) actualizada))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Función con id " + id + " no encontrada")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> errorValidacion(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflictoBaseDeDatos(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El nombre ya está registrado o el registro tiene datos asociados"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (funcionService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Función eliminada correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Función con id " + id + " no encontrada"));
    }
}
