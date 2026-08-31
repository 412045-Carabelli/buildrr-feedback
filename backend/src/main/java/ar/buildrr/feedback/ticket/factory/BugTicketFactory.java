package ar.buildrr.feedback.ticket.factory;

import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.entity.TipoTicket;
import ar.buildrr.feedback.ticket.estado.NuevoEstado;
import ar.buildrr.feedback.ticket.exception.TicketInvalidoException;
import org.springframework.stereotype.Component;

@Component
public class BugTicketFactory implements TicketFactory {

  @Override
  public TipoTicket tipoSoportado() {
    return TipoTicket.BUG;
  }

  @Override
  public Ticket crear(TicketRequest request, String creadoPor) {
    // Un bug sin descripción de qué pasa/cómo reproducirlo no sirve para
    // arrancar a trabajarlo — a diferencia de una función nueva, acá es
    // obligatorio.
    if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
      throw new TicketInvalidoException(
          "Un bug necesita descripción: qué pasa y cómo reproducirlo");
    }

    return Ticket.builder()
        .tipo(TipoTicket.BUG)
        .producto(request.getProducto())
        .titulo(request.getTitulo())
        .descripcion(request.getDescripcion())
        .estado(NuevoEstado.NOMBRE)
        .creadoPor(creadoPor)
        .build();
  }
}
