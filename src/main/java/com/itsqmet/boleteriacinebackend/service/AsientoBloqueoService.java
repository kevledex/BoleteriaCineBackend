package com.itsqmet.boleteriacinebackend.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class AsientoBloqueoService {

    private static final long DURACION_BLOQUEO_SEGUNDOS = 5 * 60;

    private record Bloqueo(String clienteId, Instant expiraEn) {}
    private record EstadoAsiento(String id, String estado, String clienteId) {}

    private final Map<Long, Map<String, Bloqueo>> bloqueosPorFuncion = new ConcurrentHashMap<>();
    private final Map<String, String> clientePorSesion = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate mensajeria;

    public AsientoBloqueoService(SimpMessagingTemplate mensajeria) {
        this.mensajeria = mensajeria;
    }

    public void registrarSesion(String sessionId, String clienteId) {
        clientePorSesion.put(sessionId, clienteId);
    }

    public synchronized boolean intentarSeleccionar(Long funcionId, String idAsiento, String clienteId) {
        Map<String, Bloqueo> bloqueos = bloqueosPorFuncion.computeIfAbsent(funcionId, id -> new ConcurrentHashMap<>());
        Bloqueo actual = bloqueos.get(idAsiento);
        if (actual != null && !actual.clienteId().equals(clienteId) && actual.expiraEn().isAfter(Instant.now())) {
            return false;
        }
        bloqueos.put(idAsiento, new Bloqueo(clienteId, Instant.now().plusSeconds(DURACION_BLOQUEO_SEGUNDOS)));
        return true;
    }

    public void liberar(Long funcionId, String idAsiento, String clienteId) {
        Map<String, Bloqueo> bloqueos = bloqueosPorFuncion.get(funcionId);
        if (bloqueos == null) return;
        Bloqueo actual = bloqueos.get(idAsiento);
        if (actual != null && actual.clienteId().equals(clienteId)) {
            bloqueos.remove(idAsiento);
        }
    }

    public void liberarPorVenta(Long funcionId, String idAsiento) {
        Map<String, Bloqueo> bloqueos = bloqueosPorFuncion.get(funcionId);
        if (bloqueos != null) bloqueos.remove(idAsiento);
    }

    public String propietario(Long funcionId, String idAsiento) {
        Map<String, Bloqueo> bloqueos = bloqueosPorFuncion.get(funcionId);
        Bloqueo actual = bloqueos == null ? null : bloqueos.get(idAsiento);
        return actual == null ? null : actual.clienteId();
    }

    public Map<String, String> obtenerReservados(Long funcionId) {
        Map<String, Bloqueo> bloqueos = bloqueosPorFuncion.get(funcionId);
        if (bloqueos == null) return Map.of();

        Instant ahora = Instant.now();
        Map<String, String> reservados = new HashMap<>();
        bloqueos.forEach((idAsiento, bloqueo) -> {
            if (bloqueo.expiraEn().isAfter(ahora)) reservados.put(idAsiento, bloqueo.clienteId());
        });
        return reservados;
    }

    @EventListener
    public void alDesconectarse(SessionDisconnectEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        String clienteId = clientePorSesion.remove(sessionId);
        if (clienteId == null) return;

        bloqueosPorFuncion.forEach((funcionId, bloqueos) ->
            bloqueos.entrySet().removeIf(entrada -> {
                boolean esDelCliente = entrada.getValue().clienteId().equals(clienteId);
                if (esDelCliente) publicar(funcionId, entrada.getKey(), "LIBRE", null);
                return esDelCliente;
            })
        );
    }

    @Scheduled(fixedDelay = 15000)
    public void liberarExpirados() {
        Instant ahora = Instant.now();
        bloqueosPorFuncion.forEach((funcionId, bloqueos) ->
            bloqueos.entrySet().removeIf(entrada -> {
                boolean expirado = entrada.getValue().expiraEn().isBefore(ahora);
                if (expirado) publicar(funcionId, entrada.getKey(), "LIBRE", null);
                return expirado;
            })
        );
    }

    public void publicar(Long funcionId, String idAsiento, String estado, String clienteId) {
        mensajeria.convertAndSend("/topic/sala/" + funcionId, new EstadoAsiento(idAsiento, estado, clienteId));
    }
}
