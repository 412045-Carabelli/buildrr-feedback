package ar.buildrr.feedback.ticket.factory;

import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.entity.TipoTicket;

/**
 * Factory Method: cada tipo de ticket arma el entity con sus propias reglas
 * (ver BugTicketFactory vs FuncionNuevaTicketFactory) en vez de un if/switch
 * en el service. Mismo criterio que pedido/precio y cuentacorriente en FrezCo.
 */
public interface TicketFactory {

  TipoTicket tipoSoportado();

  Ticket crear(TicketRequest request, String creadoPor);
}
