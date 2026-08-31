package ar.buildrr.feedback.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoTicket tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Producto producto;

  @Column(nullable = false, length = 255)
  private String titulo;

  @Column(columnDefinition = "NVARCHAR(MAX)")
  private String descripcion;

  /**
   * Nombre del estado (NUEVO, EN_PROGRESO, TESTING, COMPLETADO). Se guarda como
   * String porque lo valida y lo mueve EstadoTicket (patrón State), no un enum
   * con lógica propia. Ver ticket/estado/.
   */
  @Column(nullable = false, length = 20)
  private String estado;

  @Column(name = "creado_por", nullable = false, length = 100)
  private String creadoPor;

  @Column(name = "creado_en")
  private Instant creadoEn;

  @Column(name = "ultima_actualizacion")
  private Instant ultimaActualizacion;

  @PrePersist
  protected void onCreate() {
    this.creadoEn = Instant.now();
    this.ultimaActualizacion = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.ultimaActualizacion = Instant.now();
  }
}
