package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.DetalleSnack;
import com.itsqmet.boleteriacinebackend.repository.DetalleSnackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleSnackService {

    @Autowired
    private DetalleSnackRepository detalleSnackRepository;

    public List<DetalleSnack> obtenerPorCompra(Long compraId) {
        return detalleSnackRepository.findByCompraId(compraId);
    }
}
