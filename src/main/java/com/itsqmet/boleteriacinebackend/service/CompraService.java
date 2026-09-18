package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Compra;
import com.itsqmet.boleteriacinebackend.model.DetalleBoleto;
import com.itsqmet.boleteriacinebackend.model.DetalleSnack;
import com.itsqmet.boleteriacinebackend.model.Funcion;
import com.itsqmet.boleteriacinebackend.repository.CompraRepository;
import com.itsqmet.boleteriacinebackend.repository.DetalleBoletoRepository;
import com.itsqmet.boleteriacinebackend.repository.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    @Autowired
    private DetalleBoletoRepository detalleBoletoRepository;

    public List<Compra> obtenerTodo() {
        return compraRepository.findAll();
    }

    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findById(id);
    }

    public List<Compra> obtenerPorUsuario(Long usuarioId) {
        return compraRepository.findByUsuarioId(usuarioId);
    }

    public Optional<String> crearCompra(Compra compra) {
        List<DetalleBoleto> boletos = compra.getDetallesBoleto() != null ? compra.getDetallesBoleto() : Collections.emptyList();
        List<DetalleSnack> snacks = compra.getDetallesSnack() != null ? compra.getDetallesSnack() : Collections.emptyList();

        for (DetalleBoleto detalle : boletos) {
            boolean ocupado = detalleBoletoRepository.existsByFuncionIdAndAsientoId(
                    detalle.getFuncion().getId(), detalle.getAsiento().getId());
            if (ocupado) {
                return Optional.of("El asiento " + detalle.getAsiento().getId() + " ya está ocupado para esa función");
            }
        }

        double totalBoletos = 0;
        for (DetalleBoleto detalle : boletos) {
            Funcion funcion = funcionRepository.findById(detalle.getFuncion().getId())
                    .orElseThrow(() -> new IllegalArgumentException("La función no existe"));
            detalle.setFuncion(funcion);
            detalle.setPrecio(funcion.getPrecioBase());
            detalle.setCompra(compra);
            totalBoletos += detalle.getPrecio();
        }

        double totalSnacks = 0;
        for (DetalleSnack detalle : snacks) {
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad());
            detalle.setCompra(compra);
            totalSnacks += detalle.getSubtotal();
        }

        compra.setTotal(totalBoletos + totalSnacks);
        compra.setFechaCompra(LocalDateTime.now());
        compra.setEstado("PENDIENTE");

        compraRepository.save(compra);
        return Optional.empty();
    }

    public Optional<Compra> confirmarPago(Long id) {
        return compraRepository.findById(id).map(compra -> {
            compra.setEstado("PAGADA");
            return compraRepository.save(compra);
        });
    }

    public Optional<Compra> cancelar(Long id) {
        return compraRepository.findById(id).map(compra -> {
            compra.setEstado("CANCELADA");
            compra.getDetallesBoleto().clear();
            return compraRepository.save(compra);
        });
    }

    public boolean eliminar(Long id) {
        if (compraRepository.existsById(id)) {
            compraRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
