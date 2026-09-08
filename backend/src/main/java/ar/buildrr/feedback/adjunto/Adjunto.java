package ar.buildrr.feedback.adjunto;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "adjunto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adjunto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ticket_id", nullable = false)
  private Long ticketId;

  @Column(name = "historial_estado_id")
  private Long historialEstadoId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoAdjunto tipo;

  /**
   * `id_documento` en documentos-service (el servicio compartido de SGO que
   * guarda el archivo de verdad) — NO una URL ni un object key propio, este
   * backend no tiene bucket. La descarga es un proxy (ver
   * AdjuntoServiceImpl.descargar / AdjuntoController).
   */
  @Column(nullable = false, length = 500)
  private String url;

  @Column(name = "nombre_original", length = 255)
  private String nombreOriginal;

  @Column(name = "content_type", length = 100)
  private String contentType;

  @Column(name = "subido_por", nullable = false, length = 100)
  private String subidoPor;

  @Column(name = "subido_en")
  private Instant subidoEn;

  @PrePersist
  protected void onCreate() {
    this.subidoEn = Instant.now();
  }
}
