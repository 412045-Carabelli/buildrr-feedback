package ar.buildrr.feedback.ticket.dto;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.entity.TipoTicket;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {
  private Long id;
  private TipoTicket tipo;
  private Producto producto;
  private String titulo;
  private String modulo;
  private LocalDate fecha;
  private String descripcion;
  private String estado;
  private String creadoPor;
  private Instant creadoEn;
  private Instant ultimaActualizacion;
}
