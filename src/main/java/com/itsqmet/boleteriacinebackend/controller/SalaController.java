package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.model.Sala;
import com.itsqmet.boleteriacinebackend.service.AsientoService;
import com.itsqmet.boleteriacinebackend.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private AsientoService asientoService;

    @GetMapping
    public ResponseEntity<List<Sala>> obtenerTodo() {
        return ResponseEntity.ok(salaService.obtenerTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return salaService.buscarPorId(id)
                .map(sala -> ResponseEntity.ok((Object) sala))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Sala con id " + id + " no encontrada")));
    }

    @GetMapping("/{id}/asientos")
    public ResponseEntity<?> obtenerAsientos(@PathVariable Long id) {
        if (salaService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Sala con id " + id + " no encontrada"));
        }
        return ResponseEntity.ok(asientoService.obtenerPorSala(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Sala sala, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Sala nueva = salaService.crearSala(sala);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Sala sala, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        return salaService.actualizar(id, sala)
                .map(actualizada -> ResponseEntity.ok((Object) actualizada))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Sala con id " + id + " no encontrada")));
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
        if (salaService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Sala eliminada correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Sala con id " + id + " no encontrada"));
    }
}
