package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.ticket.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAplicacionRepository extends JpaRepository<UsuarioAplicacion, Long> {
  List<UsuarioAplicacion> findByUsername(String username);

  Optional<UsuarioAplicacion> findByUsernameAndProducto(String username, Producto producto);
}
