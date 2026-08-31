package ar.buildrr.feedback.ticket.estado;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EnProgresoEstado implements EstadoTicket {
  public static final String NOMBRE = "EN_PROGRESO";

  @Override
  public String nombre() {
    return NOMBRE;
  }

  @Override
  public Set<String> transicionesPermitidas() {
    return Set.of(TestingEstado.NOMBRE);
  }
}
