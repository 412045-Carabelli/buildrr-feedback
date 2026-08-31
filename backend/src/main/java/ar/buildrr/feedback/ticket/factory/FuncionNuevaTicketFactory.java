package ar.buildrr.feedback.ticket.factory;

import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.entity.Ticket;
import ar.buildrr.feedback.ticket.entity.TipoTicket;
import ar.buildrr.feedback.ticket.estado.NuevoEstado;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FuncionNuevaTicketFactory implements TicketFactory {

  private static final String DESCRIPCION_POR_DEFECTO =
      "Sin descripción adicional — ver título.";

  @Override
  public TipoTicket tipoSoportado() {
    return TipoTicket.FUNCION_NUEVA;
  }

  @Override
  public Ticket crear(TicketRequest request, String creadoPor) {
    // Una función nueva puede describirse solo con el título; a diferencia de
    // un bug, no es obligatorio detallar pasos de reproducción.
    String descripcion = TicketFactoryUtil.esDescripcionVacia(request.getDescripcion())
        ? DESCRIPCION_POR_DEFECTO
        : request.getDescripcion();

    return Ticket.builder()
        .tipo(TipoTicket.FUNCION_NUEVA)
        .producto(request.getProducto())
        .titulo(request.getTitulo())
        .modulo(request.getModulo())
        .fecha(request.getFecha() != null ? request.getFecha() : LocalDate.now())
        .descripcion(descripcion)
        .estado(NuevoEstado.NOMBRE)
        .creadoPor(creadoPor)
        .build();
  }
}
