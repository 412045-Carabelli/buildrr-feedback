package ar.buildrr.feedback.adjunto;

import ar.buildrr.feedback.adjunto.dto.AdjuntoResponse;
import ar.buildrr.feedback.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdjuntoController {

  private final AdjuntoService service;

  @PostMapping("/api/tickets/{ticketId}/adjuntos")
  public ResponseEntity<AdjuntoResponse> subir(
      @PathVariable Long ticketId,
      @RequestParam("archivo") MultipartFile archivo,
      @RequestParam(value = "historialEstadoId", required = false) Long historialEstadoId,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.subir(ticketId, historialEstadoId, archivo, usuario.username()));
  }

  @GetMapping("/api/tickets/{ticketId}/adjuntos")
  public ResponseEntity<List<AdjuntoResponse>> listarPorTicket(@PathVariable Long ticketId) {
    return ResponseEntity.ok(service.listarPorTicket(ticketId));
  }

  @GetMapping("/api/adjuntos/{id}/descargar")
  public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
    AdjuntoDescarga descarga = service.descargar(id);
    MediaType contentType = descarga.contentType() != null
        ? MediaType.parseMediaType(descarga.contentType())
        : MediaType.APPLICATION_OCTET_STREAM;

    return ResponseEntity.ok()
        .contentType(contentType)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(descarga.nombreArchivo()).build().toString())
        .body(descarga.contenido());
  }

  @DeleteMapping("/api/adjuntos/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser usuario) {
    service.eliminar(id, usuario.username());
    return ResponseEntity.noContent().build();
  }
}
