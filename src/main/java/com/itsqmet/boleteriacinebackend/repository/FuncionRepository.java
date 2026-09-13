package com.itsqmet.boleteriacinebackend.repository;

import com.itsqmet.boleteriacinebackend.model.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FuncionRepository extends JpaRepository<Funcion, Long> {

    List<Funcion> findByPeliculaId(String peliculaId);
    List<Funcion> findByPeliculaIdAndFecha(String peliculaId, LocalDate fecha);
    List<Funcion> findBySalaId(Long salaId);
}
