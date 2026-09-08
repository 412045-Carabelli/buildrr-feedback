package ar.buildrr.feedback.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Conteos para el dashboard del admin — ver docs/03-ciclo-de-vida.md para los 4 estados. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasTicketResponse {
  private long total;
  private long nuevos;
  private long enProgreso;
  private long testing;
  private long completados;
  /** nuevos + enProgreso + testing — todo lo que todavía no cerró. */
  private long pendientes;
}
