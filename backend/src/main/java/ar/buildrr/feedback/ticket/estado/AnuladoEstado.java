package ar.buildrr.feedback.ticket.estado;

import org.springframework.stereotype.Component;

import java.util.Set;

/** Pedido explícito del owner — cancelar un ticket que no se va a hacer. Terminal, sin reapertura. */
@Component
public class AnuladoEstado implements EstadoTicket {
  public static final String NOMBRE = "ANULADO";

  @Override
  public String nombre() {
    return NOMBRE;
  }

  @Override
  public Set<String> transicionesPermitidas() {
    return Set.of();
  }
}
