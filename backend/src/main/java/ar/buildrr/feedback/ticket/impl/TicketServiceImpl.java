package ar.buildrr.feedback.ticket.impl;

import ar.buildrr.feedback.ticket.TicketService;
import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.EditarTicketRequest;
import ar.buildrr.feedback.ticket.dto.EstadisticasTicketResponse;
import ar.buildrr.feedback.ticket.dto.HistorialEstadoResponse;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.HistorialEstado;
import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.estado.AnuladoEstado;
import ar.buildrr.feedback.ticket.estado.CompletadoEstado;
import ar.buildrr.feedback.ticket.estado.EnProgresoEstado;
import ar.buildrr.feedback.ticket.estado.EstadoTicketResolver;
import ar.buildrr.feedback.ticket.estado.NuevoEstado;
import ar.buildrr.feedback.ticket.estado.TestingEstado;
import ar.buildrr.feedback.ticket.exception.AccesoDenegadoException;
import ar.buildrr.feedback.ticket.exception.TicketInvalidoException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.ticket.factory.TicketFactory;
import ar.buildrr.feedback.ticket.factory.TicketFactoryResolver;
import ar.buildrr.feedback.ticket.repository.HistorialEstadoRepository;
import ar.buildrr.feedback.ticket.repository.TicketRepository;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionService;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import ar.buildrr.feedback.adjunto.AdjuntoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

  private final TicketRepository ticketRepository;
  private final HistorialEstadoRepository historialEstadoRepository;
  private final TicketFactoryResolver ticketFactoryResolver;
  private final EstadoTicketResolver estadoTicketResolver;
  private final UsuarioAplicacionService usuarioAplicacionService;
  private final AdjuntoService adjuntoService;

  @Override
  public TicketResponse crearConAdjunto(TicketRequest request, List<MultipartFile> archivos, String creadoPor) {
    Ticket guardado = crearTicket(request, creadoPor);

    if (archivos != null) {
      for (MultipartFile archivo : archivos) {
        if (archivo == null || archivo.isEmpty()) continue;
        // Misma transacción que la creación del ticket: si documentos-service
        // rechaza o falla la subida de cualquiera, se revierte el ticket
        // también — no queda un ticket a medio adjuntar.
        adjuntoService.subir(guardado.getId(), null, archivo, creadoPor);
      }
    }

    return toResponse(guardado);
  }

  private Ticket crearTicket(TicketRequest request, String creadoPor) {
    if (!usuarioAplicacionService.tieneAcceso(creadoPor, request.getProducto())) {
      throw new AccesoDenegadoException("No tenés acceso a " + request.getProducto());
    }

    TicketFactory factory = ticketFactoryResolver.resolver(request.getTipo());
    Ticket ticket = factory.crear(request, creadoPor);
    Ticket guardado = ticketRepository.save(ticket);

    historialEstadoRepository.save(HistorialEstado.builder()
        .ticketId(guardado.getId())
        .estadoAnterior(null)
        .estadoNuevo(guardado.getEstado())
        .cambiadoPor(creadoPor)
        .build());

    return guardado;
  }

  @Override
  @Transactional(readOnly = true)
  public TicketResponse obtenerPorId(Long id) {
    return toResponse(buscar(id));
  }

  @Override
  public TicketResponse editar(Long id, EditarTicketRequest request, String editor) {
    Ticket ticket = buscar(id);

    boolean esCreador = ticket.getCreadoPor().equals(editor);
    if (!esCreador && !usuarioAplicacionService.esAdmin(editor, ticket.getProducto())) {
      throw new AccesoDenegadoException("Solo quien creó el ticket o un admin de " + ticket.getProducto() + " puede editarlo");
    }
    if (AnuladoEstado.NOMBRE.equals(ticket.getEstado())) {
      throw new TicketInvalidoException("Un ticket anulado no se puede editar");
    }

    ticket.setTitulo(request.getTitulo());
    ticket.setModulo(request.getModulo());
    ticket.setFecha(request.getFecha() != null ? request.getFecha() : ticket.getFecha());
    ticket.setDescripcion(request.getDescripcion());

    return toResponse(ticketRepository.save(ticket));
  }

  @Override
  @Transactional(readOnly = true)
  public List<HistorialEstadoResponse> historial(Long id) {
    buscar(id);
    return historialEstadoRepository.findByTicketIdOrderByCambiadoEnAsc(id).stream()
        .map(h -> HistorialEstadoResponse.builder()
            .estadoAnterior(h.getEstadoAnterior())
            .estadoNuevo(h.getEstadoNuevo())
            .nota(h.getNota())
            .cambiadoPor(h.getCambiadoPor())
            .cambiadoEn(h.getCambiadoEn())
            .build())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketResponse> listar(String username, Producto filtroProducto, List<String> filtroEstados) {
    Set<Producto> accesibles = accesibles(username, filtroProducto);
    if (accesibles.isEmpty()) {
      return List.of();
    }
    boolean hayFiltroEstado = filtroEstados != null && !filtroEstados.isEmpty();

    if (filtroProducto != null) {
      List<Ticket> tickets = hayFiltroEstado
          ? ticketRepository.findByProductoAndEstadoIn(filtroProducto, filtroEstados)
          : ticketRepository.findByProducto(filtroProducto);
      return tickets.stream().map(this::toResponse).toList();
    }

    List<Ticket> tickets = hayFiltroEstado
        ? ticketRepository.findByProductoInAndEstadoIn(accesibles, filtroEstados)
        : ticketRepository.findByProductoIn(accesibles);
    return tickets.stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EstadisticasTicketResponse estadisticas(String username, Producto filtroProducto) {
    Set<Producto> accesibles = accesibles(username, filtroProducto);
    if (accesibles.isEmpty()) {
      return EstadisticasTicketResponse.builder().build();
    }

    long nuevos = contar(accesibles, filtroProducto, NuevoEstado.NOMBRE);
    long enProgreso = contar(accesibles, filtroProducto, EnProgresoEstado.NOMBRE);
    long testing = contar(accesibles, filtroProducto, TestingEstado.NOMBRE);
    long completados = contar(accesibles, filtroProducto, CompletadoEstado.NOMBRE);

    return EstadisticasTicketResponse.builder()
        .total(nuevos + enProgreso + testing + completados)
        .nuevos(nuevos)
        .enProgreso(enProgreso)
        .testing(testing)
        .completados(completados)
        .pendientes(nuevos + enProgreso + testing)
        .build();
  }

  /** Set de productos a mirar: solo el filtrado (si el usuario tiene acceso) o todos los accesibles. */
  private Set<Producto> accesibles(String username, Producto filtroProducto) {
    Set<Producto> accesibles = usuarioAplicacionService.misAplicaciones(username).stream()
        .map(AplicacionAccesoResponse::getProducto)
        .collect(Collectors.toSet());

    if (filtroProducto == null) {
      return accesibles;
    }
    if (!accesibles.contains(filtroProducto)) {
      throw new AccesoDenegadoException("No tenés acceso a " + filtroProducto);
    }
    return Set.of(filtroProducto);
  }

  private long contar(Set<Producto> accesibles, Producto filtroProducto, String estado) {
    return filtroProducto != null
        ? ticketRepository.countByProductoAndEstado(filtroProducto, estado)
        : ticketRepository.countByProductoInAndEstado(accesibles, estado);
  }

  @Override
  public TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor) {
    Ticket ticket = buscar(id);

    boolean esAdmin = usuarioAplicacionService.esAdmin(cambiadoPor, ticket.getProducto());
    if (!esAdmin && !puedeCreadorValidarTesting(ticket, request.getEstadoNuevo(), cambiadoPor)) {
      throw new AccesoDenegadoException("Solo un admin de " + ticket.getProducto() + " puede cambiar el estado");
    }

    String estadoAnterior = ticket.getEstado();
    estadoTicketResolver.validarTransicion(estadoAnterior, request.getEstadoNuevo());

    ticket.setEstado(request.getEstadoNuevo());
    Ticket guardado = ticketRepository.save(ticket);

    historialEstadoRepository.save(HistorialEstado.builder()
        .ticketId(id)
        .estadoAnterior(estadoAnterior)
        .estadoNuevo(request.getEstadoNuevo())
        .nota(request.getNota())
        .cambiadoPor(cambiadoPor)
        .build());

    return toResponse(guardado);
  }

  /**
   * Excepción puntual al esquema admin-only: el creador del ticket puede
   * validar el resultado de un TESTING sin ser admin — confirmar
   * (COMPLETADO) o rechazar (vuelve a EN_PROGRESO). No habilita ninguna otra
   * transición (p. ej. ANULADO sigue siendo solo de admin).
   */
  private boolean puedeCreadorValidarTesting(Ticket ticket, String estadoNuevo, String solicitante) {
    return TestingEstado.NOMBRE.equals(ticket.getEstado())
        && ticket.getCreadoPor().equals(solicitante)
        && (CompletadoEstado.NOMBRE.equals(estadoNuevo) || EnProgresoEstado.NOMBRE.equals(estadoNuevo));
  }

  private Ticket buscar(Long id) {
    return ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException("Ticket " + id + " no existe"));
  }

  private TicketResponse toResponse(Ticket ticket) {
    return TicketResponse.builder()
        .id(ticket.getId())
        .tipo(ticket.getTipo())
        .producto(ticket.getProducto())
        .titulo(ticket.getTitulo())
        .modulo(ticket.getModulo())
        .fecha(ticket.getFecha())
        .descripcion(ticket.getDescripcion())
        .estado(ticket.getEstado())
        .creadoPor(ticket.getCreadoPor())
        .creadoEn(ticket.getCreadoEn())
        .ultimaActualizacion(ticket.getUltimaActualizacion())
        .build();
  }
}
