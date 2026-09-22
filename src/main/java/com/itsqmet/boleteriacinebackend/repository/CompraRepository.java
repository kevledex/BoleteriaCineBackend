package com.itsqmet.boleteriacinebackend.repository;

import com.itsqmet.boleteriacinebackend.model.Compra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {
  List<Compra> findByUsuarioId(Long usuarioId);

  List<Compra> findByUsuarioEmailAndEstadoOrderByFechaCompraDesc(
    String email,
    String estado
  );
}
