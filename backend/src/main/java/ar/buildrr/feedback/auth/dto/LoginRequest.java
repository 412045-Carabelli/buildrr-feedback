package ar.buildrr.feedback.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
  @NotBlank(message = "Usuario requerido")
  private String usuario;

  @NotBlank(message = "Contraseña requerida")
  private String password;
}
