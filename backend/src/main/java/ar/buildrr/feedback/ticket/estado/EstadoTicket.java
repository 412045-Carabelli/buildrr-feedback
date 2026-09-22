package ar.buildrr.feedback.ticket.estado;

import java.util.Set;

/**
 * Patrón State: cada estado del ciclo de vida sabe a qué otros estados puede
 * moverse. Ver docs/03-ciclo-de-vida.md para el diagrama que esto implementa.
 * No hay comportamiento adicional por estado (no hace falta acá), solo la
 * regla de transición — si en el futuro cada estado necesita lógica propia
 * (ej. notificar), este es el lugar para agregarla sin tocar el service.
 */
public interface EstadoTicket {

  String nombre();

  Set<String> transicionesPermitidas();

  default boolean puedeTransicionarA(String estadoDestino) {
    return transicionesPermitidas().contains(estadoDestino);
  }

  /** Transiciones donde el motivo es obligatorio (ej. "no funciona" desde TESTING) — por defecto ninguna. */
  default boolean requiereNota(String estadoDestino) {
    return false;
  }
}
