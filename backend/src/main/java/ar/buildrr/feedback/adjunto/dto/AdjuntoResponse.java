package ar.buildrr.feedback.adjunto.dto;

import ar.buildrr.feedback.adjunto.TipoAdjunto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdjuntoResponse {
  private Long id;
  private Long ticketId;
  private Long historialEstadoId;
  private TipoAdjunto tipo;
  private String nombreOriginal;
  /** Para que el frontend decida si lo abre en una pestaña nueva (imagen/PDF) o fuerza la descarga. */
  private String contentType;
  private String subidoPor;
  private Instant subidoEn;

  /**
   * Ruta relativa a este backend para descargarlo (GET, requiere el mismo JWT
   * que todo lo demás). No es una URL de MinIO — el bucket es privado y su
   * hostname interno (minio:9000) no es alcanzable desde el navegador.
   */
  private String urlDescarga;
}
