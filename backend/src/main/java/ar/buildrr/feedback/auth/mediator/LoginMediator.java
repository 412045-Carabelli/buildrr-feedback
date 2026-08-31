package ar.buildrr.feedback.auth.mediator;

import ar.buildrr.feedback.auth.dto.LoginRequest;
import ar.buildrr.feedback.auth.dto.LoginResponse;
import ar.buildrr.feedback.auth.exception.CredencialesInvalidasException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Único punto de entrada del login. Recorre los colleagues en orden (@Order en
 * cada uno: FrezCo primero por ser un chequeo local barato, SGO como
 * fallback) y delega en el primero que "soporta" el request. Los colleagues no
 * se conocen entre sí — agregar una tercera fuente de identidad es agregar una
 * clase, no tocar esto ni los otros colleagues.
 */
@Component
public class LoginMediator {

  private final List<AuthColleague> colleagues;

  public LoginMediator(List<AuthColleague> colleagues) {
    this.colleagues = colleagues;
  }

  public LoginResponse autenticar(LoginRequest request) {
    return colleagues.stream()
        .filter(colleague -> colleague.soporta(request))
        .findFirst()
        .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"))
        .autenticar(request);
  }
}
