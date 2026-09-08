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
    // Reabrir el ciclo (pedido explícito del owner) — el frontend confirma
    // con el usuario antes de mandar esta transición, acá solo se valida que
    // sea válida. Sigue sin haber "cancelado/descartado" en el alcance.
    return Set.of(NuevoEstado.NOMBRE);
  }
}
