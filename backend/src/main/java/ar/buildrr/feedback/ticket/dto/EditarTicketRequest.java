package ar.buildrr.feedback.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Edita los datos de carga del ticket — no el tipo ni el producto (eso no se reasigna). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarTicketRequest {
  @NotBlank(message = "Título requerido")
  private String titulo;

  private String modulo;

  private LocalDate fecha;

  private String descripcion;
}
