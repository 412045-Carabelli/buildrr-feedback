package ar.buildrr.feedback.ticket.impl;

import ar.buildrr.feedback.ticket.TicketService;
import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.HistorialEstado;
import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.estado.EstadoTicketResolver;
import ar.buildrr.feedback.ticket.exception.AccesoDenegadoException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.ticket.factory.TicketFactory;
import ar.buildrr.feedback.ticket.factory.TicketFactoryResolver;
import ar.buildrr.feedback.ticket.repository.HistorialEstadoRepository;
import ar.buildrr.feedback.ticket.repository.TicketRepository;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionService;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Override
  public TicketResponse crear(TicketRequest request, String creadoPor) {
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

    return toResponse(guardado);
  }

  @Override
  @Transactional(readOnly = true)
  public TicketResponse obtenerPorId(Long id) {
    return toResponse(buscar(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketResponse> listar(String username, Producto filtroProducto) {
    Set<Producto> accesibles = usuarioAplicacionService.misAplicaciones(username).stream()
        .map(AplicacionAccesoResponse::getProducto)
        .collect(Collectors.toSet());

    if (filtroProducto != null) {
      if (!accesibles.contains(filtroProducto)) {
        throw new AccesoDenegadoException("No tenés acceso a " + filtroProducto);
      }
      return ticketRepository.findByProducto(filtroProducto).stream().map(this::toResponse).toList();
    }

    if (accesibles.isEmpty()) {
      return List.of();
    }
    return ticketRepository.findByProductoIn(accesibles).stream().map(this::toResponse).toList();
  }

  @Override
  public TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor) {
    Ticket ticket = buscar(id);

    if (!usuarioAplicacionService.esAdmin(cambiadoPor, ticket.getProducto())) {
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
