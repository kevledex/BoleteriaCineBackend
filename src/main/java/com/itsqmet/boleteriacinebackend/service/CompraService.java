package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.dto.EntradaUsuarioDTO;
import com.itsqmet.boleteriacinebackend.model.Asiento;
import com.itsqmet.boleteriacinebackend.model.Compra;
import com.itsqmet.boleteriacinebackend.model.DetalleBoleto;
import com.itsqmet.boleteriacinebackend.model.DetalleSnack;
import com.itsqmet.boleteriacinebackend.model.Funcion;
import com.itsqmet.boleteriacinebackend.model.Usuario;
import com.itsqmet.boleteriacinebackend.repository.AsientoRepository;
import com.itsqmet.boleteriacinebackend.repository.CompraRepository;
import com.itsqmet.boleteriacinebackend.repository.DetalleBoletoRepository;
import com.itsqmet.boleteriacinebackend.repository.FuncionRepository;
import com.itsqmet.boleteriacinebackend.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {

  @Autowired
  private CompraRepository compraRepository;

  @Autowired
  private FuncionRepository funcionRepository;

  @Autowired
  private DetalleBoletoRepository detalleBoletoRepository;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private AsientoRepository asientoRepository;

  public record SnackPedido(Long id, Integer cantidad) {}

  public record CompraSolicitud(
    Long funcionId,
    List<Long> asientoIds,
    List<SnackPedido> snacks
  ) {}

  private record Producto(String nombre, double precio) {}

  private static final Map<Long, Producto> CATALOGO = Map.ofEntries(
    Map.entry(1L, new Producto("Combo 1", 13.0)),
    Map.entry(2L, new Producto("Combo 2", 15.0)),
    Map.entry(3L, new Producto("Combo 3", 23.0)),
    Map.entry(4L, new Producto("Combo 4", 19.0)),
    Map.entry(5L, new Producto("Bebida Pequeña", 3.5)),
    Map.entry(6L, new Producto("Bebida Grande", 4.5)),
    Map.entry(7L, new Producto("Café", 3.0)),
    Map.entry(8L, new Producto("Agua Sin Gas", 2.5)),
    Map.entry(9L, new Producto("Tic Tac Naranja", 2.0)),
    Map.entry(10L, new Producto("Gomas Trolli Sour Octopus", 3.5)),
    Map.entry(11L, new Producto("Hershey's Milk Chocolate", 3.0)),
    Map.entry(12L, new Producto("M&M's Milk Chocolate", 3.0))
  );

  @Transactional
  public Compra registrarCompra(CompraSolicitud solicitud, String correo) {
    if (solicitud == null) {
      throw new IllegalArgumentException("La compra no contiene datos");
    }

    List<Long> asientoIds =
      solicitud.asientoIds() == null ? List.of() : solicitud.asientoIds();
    List<SnackPedido> snacksSolicitados =
      solicitud.snacks() == null ? List.of() : solicitud.snacks();

    boolean tieneFuncion = solicitud.funcionId() != null;
    boolean tieneAsientos = !asientoIds.isEmpty();
    boolean tieneSnacks = !snacksSolicitados.isEmpty();

    if (tieneFuncion != tieneAsientos) {
      throw new IllegalArgumentException(
        "La función y los asientos deben enviarse juntos"
      );
    }

    if (!tieneAsientos && !tieneSnacks) {
      throw new IllegalArgumentException(
        "Selecciona al menos un boleto o un producto de dulcería"
      );
    }

    Usuario usuario = usuarioRepository
      .findByEmail(correo)
      .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    Compra compra = new Compra();
    compra.setUsuario(usuario);
    compra.setMetodoPago("EFECTIVO");
    compra.setEstado("PAGADA");
    compra.setFechaCompra(LocalDateTime.now());

    List<DetalleBoleto> boletos = new ArrayList<>();
    double total = 0;

    if (tieneAsientos) {
      Funcion funcion = funcionRepository
        .findWithLockById(solicitud.funcionId())
        .orElseThrow(() ->
          new IllegalArgumentException("Función no encontrada")
        );

      LocalDateTime inicioFuncion = LocalDateTime.of(
        funcion.getFecha(),
        funcion.getHora()
      );

      if (
        !inicioFuncion.isAfter(
          LocalDateTime.now(ZoneId.of("America/Guayaquil"))
        )
      ) {
        throw new IllegalArgumentException("Esta función ya comenzó");
      }

      Map<Long, Asiento> asientosSala = new java.util.HashMap<>();

      for (Asiento asiento : asientoRepository.findBySalaId(
        funcion.getSala().getId()
      )) {
        asientosSala.put(asiento.getId(), asiento);
      }

      Set<Long> usados = new HashSet<>();

      for (Long id : asientoIds) {
        if (id == null || !usados.add(id) || !asientosSala.containsKey(id)) {
          throw new IllegalArgumentException(
            "El asiento no pertenece a la sala o está repetido"
          );
        }

        if (
          detalleBoletoRepository.existsByFuncionIdAndAsientoId(
            funcion.getId(),
            id
          )
        ) {
          throw new IllegalArgumentException(
            "Uno de los asientos ya está ocupado"
          );
        }

        DetalleBoleto detalle = new DetalleBoleto();
        detalle.setCompra(compra);
        detalle.setFuncion(funcion);
        detalle.setAsiento(asientosSala.get(id));
        detalle.setPrecio(funcion.getPrecioBase());
        boletos.add(detalle);
        total += funcion.getPrecioBase();
      }
    }

    compra.setDetallesBoleto(boletos);

    List<DetalleSnack> snacks = new ArrayList<>();

    for (SnackPedido pedido : snacksSolicitados) {
      if (pedido == null || pedido.id() == null) {
        throw new IllegalArgumentException("Producto de dulcería inválido");
      }

      Producto producto = CATALOGO.get(pedido.id());

      if (
        producto == null || pedido.cantidad() == null || pedido.cantidad() < 1
      ) {
        throw new IllegalArgumentException(
          "Producto o cantidad de dulcería inválida"
        );
      }

      DetalleSnack detalle = new DetalleSnack();
      detalle.setCompra(compra);
      detalle.setSnackId(pedido.id());
      detalle.setNombreSnack(producto.nombre());
      detalle.setPrecioUnitario(producto.precio());
      detalle.setCantidad(pedido.cantidad());
      detalle.setSubtotal(producto.precio() * pedido.cantidad());
      snacks.add(detalle);
      total += detalle.getSubtotal();
    }

    compra.setDetallesSnack(snacks);
    compra.setTotal(total);

    return compraRepository.saveAndFlush(compra);
  }

  @Transactional(readOnly = true)
  public List<EntradaUsuarioDTO> obtenerEntradasPorCorreo(String correo) {
    return compraRepository
      .findByUsuarioEmailAndEstadoOrderByFechaCompraDesc(correo, "PAGADA")
      .stream()
      .filter(compra -> compra.getDetallesBoleto() != null)
      .filter(compra -> !compra.getDetallesBoleto().isEmpty())
      .map(this::crearEntradaDTO)
      .toList();
  }

  private EntradaUsuarioDTO crearEntradaDTO(Compra compra) {
    List<DetalleBoleto> boletos = compra
      .getDetallesBoleto()
      .stream()
      .sorted(
        Comparator.comparing((DetalleBoleto detalle) ->
          detalle.getAsiento().getFila()
        ).thenComparing(detalle -> detalle.getAsiento().getNumero())
      )
      .toList();

    DetalleBoleto primerBoleto = boletos.get(0);
    Funcion funcion = primerBoleto.getFuncion();

    List<String> asientos = boletos
      .stream()
      .map(
        detalle ->
          detalle.getAsiento().getFila() + detalle.getAsiento().getNumero()
      )
      .toList();

    List<EntradaUsuarioDTO.SnackDTO> snacks = (
      compra.getDetallesSnack() == null
        ? List.<DetalleSnack>of()
        : compra.getDetallesSnack()
    )
      .stream()
      .map(detalle ->
        new EntradaUsuarioDTO.SnackDTO(
          detalle.getNombreSnack(),
          detalle.getCantidad(),
          detalle.getPrecioUnitario(),
          detalle.getSubtotal()
        )
      )
      .toList();

    return new EntradaUsuarioDTO(
      compra.getId(),
      "MC-" + compra.getId(),
      compra.getFechaCompra(),
      compra.getTotal(),
      compra.getEstado(),
      funcion.getTituloPelicula(),
      funcion.getFecha(),
      funcion.getHora(),
      funcion.getFormato(),
      funcion.getIdioma(),
      funcion.getSala().getNombre(),
      asientos,
      snacks
    );
  }

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
    List<DetalleBoleto> boletos =
      compra.getDetallesBoleto() != null
        ? compra.getDetallesBoleto()
        : Collections.emptyList();
    List<DetalleSnack> snacks =
      compra.getDetallesSnack() != null
        ? compra.getDetallesSnack()
        : Collections.emptyList();

    for (DetalleBoleto detalle : boletos) {
      boolean ocupado = detalleBoletoRepository.existsByFuncionIdAndAsientoId(
        detalle.getFuncion().getId(),
        detalle.getAsiento().getId()
      );
      if (ocupado) {
        return Optional.of(
          "El asiento " +
            detalle.getAsiento().getId() +
            " ya está ocupado para esa función"
        );
      }
    }

    double totalBoletos = 0;
    for (DetalleBoleto detalle : boletos) {
      Funcion funcion = funcionRepository
        .findById(detalle.getFuncion().getId())
        .orElseThrow(() ->
          new IllegalArgumentException("La función no existe")
        );
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
