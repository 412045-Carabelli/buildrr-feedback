package ar.buildrr.feedback.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** URL pública del api-gateway de SGO, para reenviar el login (nunca se guarda contraseña acá). */
@Configuration
@ConfigurationProperties(prefix = "sgo")
@Data
public class SgoProperties {
  private String gatewayUrl;
}
