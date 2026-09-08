package ar.buildrr.feedback.adjunto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Contrato de respuesta de documentos-service (ver DocumentoDto ahí) — solo
 * mapeamos los campos que nos importan, el resto se ignora.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentoRemotoResponse {
  private Long id_documento;
}
