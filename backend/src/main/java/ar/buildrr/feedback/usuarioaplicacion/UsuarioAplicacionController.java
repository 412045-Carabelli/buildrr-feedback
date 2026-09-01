package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.auth.AuthenticatedUser;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuario-aplicacion")
@RequiredArgsConstructor
public class UsuarioAplicacionController {

  private final UsuarioAplicacionService service;

  @GetMapping("/mis-aplicaciones")
  public ResponseEntity<List<AplicacionAccesoResponse>> misAplicaciones(@AuthenticationPrincipal AuthenticatedUser usuario) {
    return ResponseEntity.ok(service.misAplicaciones(usuario.username()));
  }
}
