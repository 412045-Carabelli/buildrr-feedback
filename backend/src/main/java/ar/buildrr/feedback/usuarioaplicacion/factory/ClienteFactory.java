package ar.buildrr.feedback.usuarioaplicacion.factory;

import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;
import org.springframework.stereotype.Component;

@Component
public class ClienteFactory implements UsuarioAplicacionFactory {

  @Override
  public RolAplicacion rolSoportado() {
    return RolAplicacion.CLIENTE;
  }

  @Override
  public UsuarioAplicacion crear(AltaUsuarioAplicacionRequest request) {
    return UsuarioAplicacion.builder()
        .username(request.getUsername())
        .producto(request.getProducto())
        .rol(RolAplicacion.CLIENTE)
        .build();
  }
}
