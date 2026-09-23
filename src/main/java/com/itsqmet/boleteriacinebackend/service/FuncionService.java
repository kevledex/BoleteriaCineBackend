package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Funcion;
import com.itsqmet.boleteriacinebackend.model.Sala;
import com.itsqmet.boleteriacinebackend.repository.FuncionRepository;
import com.itsqmet.boleteriacinebackend.repository.SalaRepository;
import com.itsqmet.boleteriacinebackend.repository.DetalleBoletoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FuncionService {
    @Autowired private FuncionRepository funcionRepository;
    @Autowired private SalaRepository salaRepository;
    @Autowired private DetalleBoletoRepository detalleBoletoRepository;

    public List<Funcion> obtenerTodo() { return funcionRepository.findAll(); }
    public Optional<Funcion> buscarPorId(Long id) { return funcionRepository.findById(id); }
    public List<Funcion> obtenerPorPelicula(Long peliculaId) { return funcionRepository.findByPeliculaId(peliculaId); }
    public List<Funcion> obtenerPorPeliculaYFecha(Long peliculaId, LocalDate fecha) {
        return funcionRepository.findByPeliculaIdAndFecha(peliculaId, fecha);
    }
    public List<Funcion> obtenerPorSala(Long salaId) { return funcionRepository.findBySalaId(salaId); }

    @Transactional
    public Funcion crearFuncion(Funcion nueva) {
        validar(nueva, null);
        return funcionRepository.save(nueva);
    }

    @Transactional
    public Optional<Funcion> actualizar(Long id, Funcion datos) {
        Optional<Funcion> existente = funcionRepository.findById(id);
        if (existente.isEmpty()) return Optional.empty();
        if (detalleBoletoRepository.existsByFuncionId(id))
            throw new IllegalArgumentException("No se puede modificar una función que ya tiene boletos");
        validar(datos, id);
        Funcion funcion = existente.get();
        funcion.setSala(datos.getSala());
        funcion.setPeliculaId(datos.getPeliculaId());
        funcion.setTituloPelicula(datos.getTituloPelicula());
        funcion.setFecha(datos.getFecha());
        funcion.setHora(datos.getHora());
        funcion.setFormato(datos.getFormato());
        funcion.setIdioma(datos.getIdioma());
        funcion.setPrecioBase(datos.getPrecioBase());
        funcion.setDuracionMinutos(datos.getDuracionMinutos());
        return Optional.of(funcionRepository.save(funcion));
    }

    private void validar(Funcion nueva, Long idActual) {
        if (nueva.getSala() == null || nueva.getSala().getId() == null)
            throw new IllegalArgumentException("Selecciona una sala existente");
        Sala sala = salaRepository.findWithLockById(nueva.getSala().getId())
                .orElseThrow(() -> new IllegalArgumentException("La sala seleccionada no existe"));
        nueva.setSala(sala);
        if (nueva.getDuracionMinutos() == null || nueva.getDuracionMinutos() < 1)
            throw new IllegalArgumentException("Indica la duración de la película en minutos");
        LocalDateTime inicio = LocalDateTime.of(nueva.getFecha(), nueva.getHora());
        LocalDateTime fin = inicio.plusMinutes(nueva.getDuracionMinutos() + 15L);
        if (!fin.toLocalDate().equals(nueva.getFecha()))
            throw new IllegalArgumentException("La película y los 15 minutos de limpieza deben terminar el mismo día");
        if (inicio.isBefore(LocalDateTime.now(ZoneId.of("America/Guayaquil"))))
            throw new IllegalArgumentException("La función debe comenzar en el futuro");
        for (Funcion otra : funcionRepository.findBySalaId(sala.getId())) {
            if (otra.getId().equals(idActual) || !otra.getFecha().equals(nueva.getFecha())) continue;
            LocalDateTime otroInicio = LocalDateTime.of(otra.getFecha(), otra.getHora());
            LocalDateTime otroFin = otroInicio.plusMinutes((otra.getDuracionMinutos() == null ? 120 : otra.getDuracionMinutos()) + 15L);
            if (inicio.isBefore(otroFin) && otroInicio.isBefore(fin))
                throw new IllegalArgumentException("La sala ya tiene una función en ese horario (incluye 15 minutos de limpieza)");
        }
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!funcionRepository.existsById(id)) return false;
        if (detalleBoletoRepository.existsByFuncionId(id))
            throw new IllegalArgumentException("No se puede eliminar una función que ya tiene boletos");
        funcionRepository.deleteById(id);
        return true;
    }
}
