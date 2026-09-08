package ar.buildrr.feedback.usuarioaplicacion.dto;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAplicacionResponse {
  private Long id;
  private String username;
  private Producto producto;
  private RolAplicacion rol;
}
