package com.itsqmet.boleteriacinebackend.repository;

import com.itsqmet.boleteriacinebackend.model.DetalleBoleto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleBoletoRepository extends JpaRepository<DetalleBoleto, Long> {

    List<DetalleBoleto> findByFuncionId(Long funcionId);
    List<DetalleBoleto> findByCompraId(Long compraId);
    boolean existsByFuncionIdAndAsientoId(Long funcionId, Long asientoId);
}
