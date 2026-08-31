package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Secret HS256 de auth-service de SGO — la única fuente de identidad del
 * ecosistema. Este backend nunca firma tokens propios, solo valida los que
 * emite SGO. No se hardcodea el valor real: viene de JWT_SECRET.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
  private String secret;
}
