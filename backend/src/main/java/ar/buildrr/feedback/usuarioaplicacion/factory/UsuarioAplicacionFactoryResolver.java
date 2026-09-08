package ar.buildrr.feedback.usuarioaplicacion.factory;

import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UsuarioAplicacionFactoryResolver {

  private final Map<RolAplicacion, UsuarioAplicacionFactory> factoriesPorRol;

  public UsuarioAplicacionFactoryResolver(List<UsuarioAplicacionFactory> factories) {
    this.factoriesPorRol = factories.stream()
        .collect(Collectors.toMap(UsuarioAplicacionFactory::rolSoportado, Function.identity()));
  }

  public UsuarioAplicacionFactory resolver(RolAplicacion rol) {
    UsuarioAplicacionFactory factory = factoriesPorRol.get(rol);
    if (factory == null) {
      throw new IllegalStateException("Sin factory para rol de aplicación: " + rol);
    }
    return factory;
  }
}
