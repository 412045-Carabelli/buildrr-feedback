package ar.buildrr.feedback.usuarioaplicacion.factory;

import ar.buildrr.feedback.ticket.exception.TicketInvalidoException;
import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminFactory implements UsuarioAplicacionFactory {

  @Override
  public RolAplicacion rolSoportado() {
    return RolAplicacion.ADMIN;
  }

  @Override
  public UsuarioAplicacion crear(AltaUsuarioAplicacionRequest request) {
    // Un ADMIN gestiona el ciclo de vida de los tickets de todo el producto —
    // a diferencia de un CLIENTE, no tiene sentido darlo de alta con un
    // username en blanco o con espacios colgando (typo típico de copiar/pegar
    // el username del JWT), porque después el match contra X-Username del
    // gateway (findByUsernameAndProducto) es exacto y silencioso: el admin
    // "existiría" en la tabla pero jamás matchearía y perdería el acceso.
    String username = request.getUsername() == null ? null : request.getUsername().trim();
    if (username == null || username.isBlank()) {
      throw new TicketInvalidoException("Username inválido para dar de alta un admin");
    }

    return UsuarioAplicacion.builder()
        .username(username)
        .producto(request.getProducto())
        .rol(RolAplicacion.ADMIN)
        .build();
  }
}
