package ar.buildrr.feedback.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "historial_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstado {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ticket_id", nullable = false)
  private Long ticketId;

  @Column(name = "estado_anterior", length = 20)
  private String estadoAnterior;

  @Column(name = "estado_nuevo", nullable = false, length = 20)
  private String estadoNuevo;

  @Column(columnDefinition = "NVARCHAR(MAX)")
  private String nota;

  @Column(name = "cambiado_por", nullable = false, length = 100)
  private String cambiadoPor;

  @Column(name = "cambiado_en")
  private Instant cambiadoEn;

  @PrePersist
  protected void onCreate() {
    this.cambiadoEn = Instant.now();
  }
}
