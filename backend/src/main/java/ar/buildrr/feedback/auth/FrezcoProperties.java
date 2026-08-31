package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Mismas credenciales (usuario/contraseña únicos) que ya usa el backend de FrezCo. */
@Configuration
@ConfigurationProperties(prefix = "frezco")
@Data
public class FrezcoProperties {
  private String appUser;
  private String appPassword;
}
