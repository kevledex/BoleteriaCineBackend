package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.DetalleBoleto;
import com.itsqmet.boleteriacinebackend.repository.DetalleBoletoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetalleBoletoService {

    @Autowired
    private DetalleBoletoRepository detalleBoletoRepository;

    public List<DetalleBoleto> obtenerPorFuncion(Long funcionId) {
        return detalleBoletoRepository.findByFuncionId(funcionId);
    }

    public List<DetalleBoleto> obtenerPorCompra(Long compraId) {
        return detalleBoletoRepository.findByCompraId(compraId);
    }

    public List<Long> obtenerAsientosOcupados(Long funcionId) {
        return detalleBoletoRepository.findByFuncionId(funcionId).stream()
                .map(detalle -> detalle.getAsiento().getId())
                .collect(Collectors.toList());
    }
}
