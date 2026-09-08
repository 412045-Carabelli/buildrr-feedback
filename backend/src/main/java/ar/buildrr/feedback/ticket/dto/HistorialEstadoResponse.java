package ar.buildrr.feedback.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoResponse {
  private String estadoAnterior;
  private String estadoNuevo;
  private String nota;
  private String cambiadoPor;
  private Instant cambiadoEn;
}
