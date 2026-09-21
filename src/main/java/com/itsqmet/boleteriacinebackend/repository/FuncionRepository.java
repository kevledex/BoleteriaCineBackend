package com.itsqmet.boleteriacinebackend.repository;

import com.itsqmet.boleteriacinebackend.model.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface FuncionRepository extends JpaRepository<Funcion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Funcion> findWithLockById(Long id);

    List<Funcion> findByPeliculaId(Long peliculaId);
    List<Funcion> findByPeliculaIdAndFecha(Long peliculaId, LocalDate fecha);
    List<Funcion> findBySalaId(Long salaId);
}
