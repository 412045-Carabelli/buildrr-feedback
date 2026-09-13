package ar.buildrr.feedback.ticket.controller;

import ar.buildrr.feedback.auth.AuthenticatedUser;
import ar.buildrr.feedback.ticket.TicketService;
import ar.buildrr.feedback.ticket.dto.CambiarEstadoRequest;
import ar.buildrr.feedback.ticket.dto.EditarTicketRequest;
import ar.buildrr.feedback.ticket.dto.EstadisticasTicketResponse;
import ar.buildrr.feedback.ticket.dto.HistorialEstadoResponse;
import ar.buildrr.feedback.ticket.dto.TicketRequest;
import ar.buildrr.feedback.ticket.dto.TicketResponse;
import ar.buildrr.feedback.ticket.entity.Producto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService service;

  /**
   * Multipart en vez de JSON: crea el ticket y sube los adjuntos (si vienen)
   * en un solo request/transacción, para no dejar tickets sin sus adjuntos
   * por una falla de red al subirlos por separado. La parte "ticket" va como
   * JSON (mismo Content-Type que un Blob armado con application/json en el
   * front).
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TicketResponse> crear(
      @Valid @RequestPart("ticket") TicketRequest request,
      @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.crearConAdjunto(request, archivos, usuario.username()));
  }

  @GetMapping
  public ResponseEntity<List<TicketResponse>> listar(
      @RequestParam(required = false) Producto producto,
      @RequestParam(required = false) List<String> estado,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.listar(usuario.username(), producto, estado));
  }

  @GetMapping("/stats")
  public ResponseEntity<EstadisticasTicketResponse> estadisticas(
      @RequestParam(required = false) Producto producto,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.estadisticas(usuario.username(), producto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TicketResponse> obtenerPorId(@PathVariable Long id) {
    return ResponseEntity.ok(service.obtenerPorId(id));
  }

  @PatchMapping("/{id}/estado")
  public ResponseEntity<TicketResponse> cambiarEstado(
      @PathVariable Long id,
      @Valid @RequestBody CambiarEstadoRequest request,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.cambiarEstado(id, request, usuario.username()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TicketResponse> editar(
      @PathVariable Long id,
      @Valid @RequestBody EditarTicketRequest request,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.editar(id, request, usuario.username()));
  }

  @GetMapping("/{id}/historial")
  public ResponseEntity<List<HistorialEstadoResponse>> historial(@PathVariable Long id) {
    return ResponseEntity.ok(service.historial(id));
  }
}
