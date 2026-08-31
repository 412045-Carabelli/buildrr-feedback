package ar.buildrr.feedback.auth.dto;

import ar.buildrr.feedback.auth.OrigenCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
  private String token;
  private OrigenCuenta origen;
}
