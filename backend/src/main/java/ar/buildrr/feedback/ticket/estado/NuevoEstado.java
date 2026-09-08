package ar.buildrr.feedback.ticket.estado;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NuevoEstado implements EstadoTicket {
  public static final String NOMBRE = "NUEVO";

  @Override
  public String nombre() {
    return NOMBRE;
  }

  @Override
  public Set<String> transicionesPermitidas() {
    return Set.of(EnProgresoEstado.NOMBRE, AnuladoEstado.NOMBRE);
  }
}
