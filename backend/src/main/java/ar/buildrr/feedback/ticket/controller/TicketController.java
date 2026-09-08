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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService service;

  @PostMapping
  public ResponseEntity<TicketResponse> crear(
      @Valid @RequestBody TicketRequest request,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.crear(request, usuario.username()));
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
