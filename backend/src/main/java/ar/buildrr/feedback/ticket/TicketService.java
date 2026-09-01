package ar.buildrr.feedback.ticket;

import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.Producto;

import java.util.List;

public interface TicketService {
  TicketResponse crear(TicketRequest request, String creadoPor);

  TicketResponse obtenerPorId(Long id);

  /** @param filtroProducto opcional — la app seleccionada en el navbar. Sin filtro, todas las que el usuario puede ver. */
  List<TicketResponse> listar(String username, Producto filtroProducto);

  TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor);
}
