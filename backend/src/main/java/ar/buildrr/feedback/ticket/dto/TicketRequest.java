package ar.buildrr.feedback.ticket.dto;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.entity.TipoTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {
  @NotNull(message = "Tipo requerido")
  private TipoTicket tipo;

  @NotNull(message = "Producto requerido")
  private Producto producto;

  @NotBlank(message = "Título requerido")
  private String titulo;

  private String modulo;

  /** Si no la manda el front, el factory usa hoy. */
  private LocalDate fecha;

  private String descripcion;
}
