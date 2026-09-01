package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.dto.AplicacionAccesoResponse;

import java.util.List;

public interface UsuarioAplicacionService {

  List<AplicacionAccesoResponse> misAplicaciones(String username);

  boolean tieneAcceso(String username, Producto producto);

  boolean esAdmin(String username, Producto producto);
}
