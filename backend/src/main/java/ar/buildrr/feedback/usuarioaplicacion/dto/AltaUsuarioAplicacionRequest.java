package ar.buildrr.feedback.usuarioaplicacion.dto;

import ar.buildrr.feedback.ticket.entity.Producto;
import ar.buildrr.feedback.usuarioaplicacion.RolAplicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltaUsuarioAplicacionRequest {
  @NotBlank(message = "Username requerido")
  private String username;

  @NotNull(message = "Producto requerido")
  private Producto producto;

  @NotNull(message = "Rol requerido")
  private RolAplicacion rol;
}
