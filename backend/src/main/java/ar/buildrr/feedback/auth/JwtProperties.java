package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mismo secret HS256 que usa auth-service de SGO. Se configura por variable de
 * entorno (JWT_SECRET), nunca se hardcodea el valor real acá.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
  private String secret;
}
