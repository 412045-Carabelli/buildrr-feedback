package ar.buildrr.feedback.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoRequest {
  @NotBlank(message = "Estado nuevo requerido")
  private String estadoNuevo;

  /** Nota visible para Pablo — ej. captura de "así quedó" o motivo de vuelta atrás. */
  private String nota;
}
