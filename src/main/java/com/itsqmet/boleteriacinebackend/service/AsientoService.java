package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Asiento;
import com.itsqmet.boleteriacinebackend.repository.AsientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsientoService {

    @Autowired
    private AsientoRepository asientoRepository;

    public List<Asiento> obtenerPorSala(Long salaId) {
        return asientoRepository.findBySalaId(salaId);
    }

    public Optional<Asiento> buscarPorId(Long id) {
        return asientoRepository.findById(id);
    }

    // codigo es fila+numero, ej. "A1", tal como lo maneja el frontend.
    public Optional<Asiento> buscarPorSalaYCodigo(Long salaId, String codigo) {
        return asientoRepository.findBySalaId(salaId).stream()
                .filter(asiento -> (asiento.getFila() + asiento.getNumero()).equals(codigo))
                .findFirst();
    }

    public Optional<Asiento> actualizar(Long id, Asiento asientoActualizado) {
        return asientoRepository.findById(id).map(asiento -> {
            asiento.setFila(asientoActualizado.getFila());
            asiento.setNumero(asientoActualizado.getNumero());
            asiento.setTipoAsiento(asientoActualizado.getTipoAsiento());
            return asientoRepository.save(asiento);
        });
    }

    public boolean eliminar(Long id) {
        if (asientoRepository.existsById(id)) {
            asientoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
