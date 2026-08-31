package ar.buildrr.feedback.ticket.estado;

import ar.buildrr.feedback.ticket.exception.TransicionInvalidaException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Junta todos los EstadoTicket que Spring encontró (uno por clase) en un mapa
 * por nombre. Agregar un estado nuevo es agregar una clase @Component, no
 * tocar este resolver ni el service.
 */
@Component
public class EstadoTicketResolver {

  private final Map<String, EstadoTicket> estadosPorNombre;

  public EstadoTicketResolver(List<EstadoTicket> estados) {
    this.estadosPorNombre = estados.stream()
        .collect(Collectors.toMap(EstadoTicket::nombre, Function.identity()));
  }

  public EstadoTicket resolver(String nombreEstado) {
    EstadoTicket estado = estadosPorNombre.get(nombreEstado);
    if (estado == null) {
      throw new TransicionInvalidaException("Estado desconocido: " + nombreEstado);
    }
    return estado;
  }

  public void validarTransicion(String estadoActual, String estadoDestino) {
    EstadoTicket actual = resolver(estadoActual);
    if (!actual.puedeTransicionarA(estadoDestino)) {
      throw new TransicionInvalidaException(
          "No se puede pasar de " + estadoActual + " a " + estadoDestino);
    }
  }
}
