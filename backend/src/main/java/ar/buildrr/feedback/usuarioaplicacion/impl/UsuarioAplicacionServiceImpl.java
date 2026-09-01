package ar.buildrr.feedback.usuarioaplicacion.impl;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionRepository;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionService;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioAplicacionServiceImpl implements UsuarioAplicacionService {

  private final UsuarioAplicacionRepository repository;

  @Override
  public List<AplicacionAccesoResponse> misAplicaciones(String username) {
    return repository.findByUsername(username).stream()
        .map(ua -> AplicacionAccesoResponse.builder().producto(ua.getProducto()).rol(ua.getRol()).build())
        .toList();
  }

  @Override
  public boolean tieneAcceso(String username, Producto producto) {
    return repository.findByUsernameAndProducto(username, producto).isPresent();
  }

  @Override
  public boolean esAdmin(String username, Producto producto) {
    return repository.findByUsernameAndProducto(username, producto)
        .map(ua -> ua.getRol() == RolAplicacion.ADMIN)
        .orElse(false);
  }
}
