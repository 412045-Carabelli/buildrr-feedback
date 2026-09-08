package ar.buildrr.feedback.usuarioaplicacion.impl;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.ticket.exception.AccesoDenegadoException;
import ar.buildrr.feedback.ticket.exception.TicketInvalidoException;
import ar.buildrr.feedback.ticket.exception.TicketNotFoundException;
import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionRepository;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacionService;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import ar.buildrr.feedback.usuarioaplicacion.dto.UsuarioAplicacionResponse;
import ar.buildrr.feedback.usuarioaplicacion.factory.UsuarioAplicacionFactoryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioAplicacionServiceImpl implements UsuarioAplicacionService {

  private final UsuarioAplicacionRepository repository;
  private final UsuarioAplicacionFactoryResolver factoryResolver;

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

  @Override
  public List<UsuarioAplicacionResponse> listarPorProducto(String solicitante, Producto producto) {
    exigirAdmin(solicitante, producto);
    return repository.findByProducto(producto).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public UsuarioAplicacionResponse darDeAlta(String solicitante, AltaUsuarioAplicacionRequest request) {
    exigirAdmin(solicitante, request.getProducto());

    if (repository.findByUsernameAndProducto(request.getUsername(), request.getProducto()).isPresent()) {
      throw new TicketInvalidoException(
          request.getUsername() + " ya tiene acceso a " + request.getProducto());
    }

    UsuarioAplicacion nuevo = factoryResolver.resolver(request.getRol()).crear(request);
    return toResponse(repository.save(nuevo));
  }

  @Override
  @Transactional
  public void revocar(String solicitante, Long id) {
    UsuarioAplicacion acceso = repository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException("Acceso " + id + " no existe"));
    exigirAdmin(solicitante, acceso.getProducto());
    repository.delete(acceso);
  }

  private void exigirAdmin(String solicitante, Producto producto) {
    if (!esAdmin(solicitante, producto)) {
      throw new AccesoDenegadoException("Solo un admin de " + producto + " puede gestionar sus usuarios");
    }
  }

  private UsuarioAplicacionResponse toResponse(UsuarioAplicacion ua) {
    return UsuarioAplicacionResponse.builder()
        .id(ua.getId())
        .username(ua.getUsername())
        .producto(ua.getProducto())
        .rol(ua.getRol())
        .build();
  }
}
