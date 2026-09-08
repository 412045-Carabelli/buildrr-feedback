package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;
import ar.buildrr.feedback.usuarioaplicacion.dto.UsuarioAplicacionResponse;

import java.util.List;

public interface UsuarioAplicacionService {

  List<AplicacionAccesoResponse> misAplicaciones(String username);

  boolean tieneAcceso(String username, Producto producto);

  boolean esAdmin(String username, Producto producto);

  /** Solo para un admin de ese producto — panel de gestión de usuarios. */
  List<UsuarioAplicacionResponse> listarPorProducto(String solicitante, Producto producto);

  /** Alta vía Factory Method por rol (ver usuarioaplicacion/factory/). Solo un admin del producto puede dar de alta. */
  UsuarioAplicacionResponse darDeAlta(String solicitante, AltaUsuarioAplicacionRequest request);

  /** Revocar un acceso. Solo un admin del producto de ese acceso. */
  void revocar(String solicitante, Long id);
}
