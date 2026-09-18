package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Funcion;
import com.itsqmet.boleteriacinebackend.repository.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FuncionService {

    @Autowired
    private FuncionRepository funcionRepository;

    public List<Funcion> obtenerTodo() {
        return funcionRepository.findAll();
    }

    public Optional<Funcion> buscarPorId(Long id) {
        return funcionRepository.findById(id);
    }

    public List<Funcion> obtenerPorPelicula(Long peliculaId) {
        return funcionRepository.findByPeliculaId(peliculaId);
    }

    public List<Funcion> obtenerPorPeliculaYFecha(Long peliculaId, LocalDate fecha) {
        return funcionRepository.findByPeliculaIdAndFecha(peliculaId, fecha);
    }

    public List<Funcion> obtenerPorSala(Long salaId) {
        return funcionRepository.findBySalaId(salaId);
    }

    public Funcion crearFuncion(Funcion funcion) {
        return funcionRepository.save(funcion);
    }

    public Optional<Funcion> actualizar(Long id, Funcion funcionActualizada) {
        return funcionRepository.findById(id).map(funcion -> {
            funcion.setSala(funcionActualizada.getSala());
            funcion.setPeliculaId(funcionActualizada.getPeliculaId());
            funcion.setTituloPelicula(funcionActualizada.getTituloPelicula());
            funcion.setFecha(funcionActualizada.getFecha());
            funcion.setHora(funcionActualizada.getHora());
            funcion.setFormato(funcionActualizada.getFormato());
            funcion.setIdioma(funcionActualizada.getIdioma());
            funcion.setPrecioBase(funcionActualizada.getPrecioBase());
            return funcionRepository.save(funcion);
        });
    }

    public boolean eliminar(Long id) {
        if (funcionRepository.existsById(id)) {
            funcionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
