package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.model.Asiento;
import com.itsqmet.boleteriacinebackend.service.AsientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/asientos")
public class AsientoController {

    @Autowired
    private AsientoService asientoService;

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return asientoService.buscarPorId(id)
                .map(asiento -> ResponseEntity.ok((Object) asiento))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Asiento con id " + id + " no encontrado")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Asiento asiento, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        return asientoService.actualizar(id, asiento)
                .map(actualizado -> ResponseEntity.ok((Object) actualizado))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Asiento con id " + id + " no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (asientoService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Asiento eliminado correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Asiento con id " + id + " no encontrado"));
    }
}
