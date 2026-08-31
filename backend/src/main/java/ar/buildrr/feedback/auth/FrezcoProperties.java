package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * `appUser` es solo para el chequeo de ruteo del Mediator (¿este login es de
 * FrezCo?) — la contraseña NUNCA se duplica acá. `FrezcoAuthColleague` valida
 * la contraseña llamando al login real de FrezCo (`loginUrl`).
 */
@Configuration
@ConfigurationProperties(prefix = "frezco")
@Data
public class FrezcoProperties {
  private String appUser;
  private String loginUrl;
}
