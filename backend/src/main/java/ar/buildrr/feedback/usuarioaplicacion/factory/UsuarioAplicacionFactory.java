package ar.buildrr.feedback.usuarioaplicacion.factory;

import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.UsuarioAplicacion;
import ar.buildrr.feedback.usuarioaplicacion.dto.AltaUsuarioAplicacionRequest;

/**
 * Factory Method: cada rol arma el acceso con sus propias reglas — mismo
 * criterio que ticket/factory/TicketFactory para tipo de ticket. Agregar un
 * rol nuevo es agregar una clase + registrarla en el enum, no tocar el
 * service.
 */
public interface UsuarioAplicacionFactory {

  RolAplicacion rolSoportado();

  UsuarioAplicacion crear(AltaUsuarioAplicacionRequest request);
}
