package ar.buildrr.feedback.ticket.estado;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CompletadoEstado implements EstadoTicket {
  public static final String NOMBRE = "COMPLETADO";

  @Override
  public String nombre() {
    return NOMBRE;
  }

  @Override
  public Set<String> transicionesPermitidas() {
    // Estado terminal — no hay "cancelado/descartado" en el alcance inicial.
    return Set.of();
  }
}
