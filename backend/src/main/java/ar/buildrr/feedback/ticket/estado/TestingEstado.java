package ar.buildrr.feedback.ticket.estado;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestingEstado implements EstadoTicket {
  public static final String NOMBRE = "TESTING";

  @Override
  public String nombre() {
    return NOMBRE;
  }

  @Override
  public Set<String> transicionesPermitidas() {
    // Desde TESTING se puede confirmar (COMPLETADO) o volver atrás si no
    // funciona (EN_PROGRESO) — ver docs/03-ciclo-de-vida.md.
    return Set.of(CompletadoEstado.NOMBRE, EnProgresoEstado.NOMBRE, AnuladoEstado.NOMBRE);
  }

  @Override
  public boolean requiereNota(String estadoDestino) {
    // "No funciona, volver a en progreso" — necesita el motivo del rechazo.
    return EnProgresoEstado.NOMBRE.equals(estadoDestino);
  }
}
