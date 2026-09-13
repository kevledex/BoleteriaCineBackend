package com.itsqmet.boleteriacinebackend.repository;

import com.itsqmet.boleteriacinebackend.model.DetalleSnack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleSnackRepository extends JpaRepository<DetalleSnack, Long> {

    List<DetalleSnack> findByCompraId(Long compraId);
}
