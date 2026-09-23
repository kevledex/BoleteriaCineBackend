package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Asiento;
import com.itsqmet.boleteriacinebackend.model.Sala;
import com.itsqmet.boleteriacinebackend.repository.AsientoRepository;
import com.itsqmet.boleteriacinebackend.repository.SalaRepository;
import com.itsqmet.boleteriacinebackend.repository.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    public List<Sala> obtenerTodo() {
        return salaRepository.findAll();
    }

    public Optional<Sala> buscarPorId(Long id) {
        return salaRepository.findById(id);
    }

    @Transactional
    public Sala crearSala(Sala sala) {
        if (sala.getFilas() > 26) throw new IllegalArgumentException("Máximo 26 filas por sala");
        Sala salaGuardada = salaRepository.save(sala);
        generarAsientos(salaGuardada);
        return salaGuardada;
    }

    public Optional<Sala> actualizar(Long id, Sala salaActualizada) {
        return salaRepository.findById(id).map(sala -> {
            sala.setNombre(salaActualizada.getNombre());
            if (!sala.getFilas().equals(salaActualizada.getFilas()) || !sala.getColumnas().equals(salaActualizada.getColumnas()))
                throw new IllegalArgumentException("Las dimensiones no pueden cambiar después de crear los asientos");
            sala.setTipoSala(salaActualizada.getTipoSala());
            return salaRepository.save(sala);
        });
    }

    public boolean eliminar(Long id) {
        if (salaRepository.existsById(id)) {
            if (!funcionRepository.findBySalaId(id).isEmpty())
                throw new IllegalArgumentException("Elimina primero las funciones de esta sala");
            salaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void generarAsientos(Sala sala) {
        List<Asiento> asientos = new ArrayList<>();
        for (int f = 0; f < sala.getFilas(); f++) {
            String fila = String.valueOf((char) ('A' + f));
            for (int numero = 1; numero <= sala.getColumnas(); numero++) {
                Asiento asiento = new Asiento();
                asiento.setSala(sala);
                asiento.setFila(fila);
                asiento.setNumero(numero);
                asiento.setTipoAsiento("NORMAL");
                asientos.add(asiento);
            }
        }
        asientoRepository.saveAll(asientos);
    }
}
