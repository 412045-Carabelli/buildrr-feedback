package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.auth.AuthenticatedUser;
import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;
import ar.buildrr.feedback.usuarioaplicacion.dto.UsuarioAplicacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Panel de owner/admin: alta y baja de usuarios por producto. Ver usuarioaplicacion/factory/. */
@RestController
@RequestMapping("/api/admin/usuarios-aplicacion")
@RequiredArgsConstructor
public class AdminUsuarioAplicacionController {

  private final UsuarioAplicacionService service;

  @GetMapping
  public ResponseEntity<List<UsuarioAplicacionResponse>> listar(
      @RequestParam Producto producto,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.listarPorProducto(usuario.username(), producto));
  }

  @PostMapping
  public ResponseEntity<UsuarioAplicacionResponse> darDeAlta(
      @Valid @RequestBody AltaUsuarioAplicacionRequest request,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.darDeAlta(usuario.username(), request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> revocar(
      @PathVariable Long id,
      @AuthenticationPrincipal AuthenticatedUser usuario) {
    service.revocar(usuario.username(), id);
    return ResponseEntity.noContent().build();
  }
}
