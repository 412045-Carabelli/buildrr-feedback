package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dos secrets HS256 posibles: `secret` es el de auth-service de SGO (tokens que
 * este backend NUNCA firma, solo valida). `ownSecret` es propio de
 * buildrr-feedback, usado únicamente para tokens de cuentas FrezCo (ver
 * auth/mediator/FrezcoAuthColleague). Ninguno se hardcodea acá: vienen de
 * JWT_SECRET / JWT_OWN_SECRET.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
  private String secret;
  private String ownSecret;
}
