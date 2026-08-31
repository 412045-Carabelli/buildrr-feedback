package ar.buildrr.feedback.ticket;

import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;

import java.util.List;

public interface TicketService {
  TicketResponse crear(TicketRequest request, String creadoPor);

  TicketResponse obtenerPorId(Long id);

  List<TicketResponse> listar();

  TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request, String cambiadoPor);
}
