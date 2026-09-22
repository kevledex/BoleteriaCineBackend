package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.service.AsientoBloqueoService;
import com.itsqmet.boleteriacinebackend.service.AsientoService;
import com.itsqmet.boleteriacinebackend.service.DetalleBoletoService;
import com.itsqmet.boleteriacinebackend.service.FuncionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

// Recibe las acciones de selección/liberación de asientos por STOMP (/app/asiento/seleccionar)
// y retransmite el estado autoritativo a todos los suscriptores de /topic/sala/{funcionId}.
// El backend nunca emite "SELECCIONADO": eso lo decide cada cliente comparando el clienteId
// que viaja en el mensaje contra el suyo propio (si coincide, es su selección; si no, es un
// asiento RESERVADO por otro usuario).
@Controller
public class AsientoWebSocketController {

    @Autowired
    private AsientoBloqueoService bloqueoService;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private FuncionService funcionService;

    @Autowired
    private DetalleBoletoService detalleBoletoService;

    public record AccionAsiento(Long funcionId, String idAsiento, String estado, String clienteId) {}

    @MessageMapping("/asiento/seleccionar")
    public void seleccionar(AccionAsiento accion, SimpMessageHeaderAccessor headerAccessor) {
        if (accion.funcionId() == null || accion.idAsiento() == null || accion.clienteId() == null) {
            return;
        }

        bloqueoService.registrarSesion(headerAccessor.getSessionId(), accion.clienteId());

        funcionService.buscarPorId(accion.funcionId()).ifPresent(funcion -> {
            if ("LIBRE".equals(accion.estado())) {
                bloqueoService.liberar(accion.funcionId(), accion.idAsiento(), accion.clienteId());
                bloqueoService.publicar(accion.funcionId(), accion.idAsiento(), "LIBRE", null);
                return;
            }

            if (estaVendido(accion.funcionId(), funcion.getSala().getId(), accion.idAsiento())) {
                bloqueoService.publicar(accion.funcionId(), accion.idAsiento(), "OCUPADO", null);
                return;
            }

            boolean obtenido = bloqueoService.intentarSeleccionar(accion.funcionId(), accion.idAsiento(), accion.clienteId());
            String propietario = obtenido ? accion.clienteId() : bloqueoService.propietario(accion.funcionId(), accion.idAsiento());
            bloqueoService.publicar(accion.funcionId(), accion.idAsiento(), "RESERVADO", propietario);
        });
    }

    private boolean estaVendido(Long funcionId, Long salaId, String idAsiento) {
        return asientoService.buscarPorSalaYCodigo(salaId, idAsiento)
                .map(asiento -> detalleBoletoService.obtenerAsientosOcupados(funcionId).contains(asiento.getId()))
                .orElse(true);
    }
}
