package ar.buildrr.feedback.auth;

import ar.buildrr.feedback.auth.dto.LoginRequest;
import ar.buildrr.feedback.auth.dto.LoginResponse;
import ar.buildrr.feedback.auth.exception.CredencialesInvalidasException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Única fuente de identidad del ecosistema: auth-service de SGO. Pablo y la
 * dueña de FrezCo son las dos, cuentas normales en esa misma base — no hay
 * nada que "rutear" entre sistemas. Reenvía el login y re-emite el JWT real
 * tal cual; este backend nunca firma tokens propios.
 *
 * Antes había un Mediator con un AuthColleague por fuente de identidad (SGO
 * y FrezCo por separado, esta última con su propio backend/JWT). Se unificó:
 * la dueña de FrezCo ahora es una cuenta más en sgo_auth (ver
 * docs/00-arquitectura.md), así que la abstracción quedó con una sola
 * implementación real — se sacó, no tiene sentido mantenerla vacía.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SgoLoginClient {

  private final RestClient.Builder restClientBuilder;
  private final SgoProperties sgoProperties;

  @SuppressWarnings("unchecked")
  public LoginResponse login(LoginRequest request) {
    try {
      Map<String, Object> respuesta = restClientBuilder.build()
          .post()
          .uri(sgoProperties.getGatewayUrl() + "/auth/login")
          .body(Map.of("email", request.getUsuario(), "password", request.getPassword()))
          .retrieve()
          .body(Map.class);

      String token = respuesta != null ? (String) respuesta.get("access_token") : null;
      if (token == null) {
        throw new CredencialesInvalidasException("SGO no devolvió token");
      }
      return LoginResponse.builder().token(token).build();

    } catch (RestClientResponseException e) {
      log.warn("Login SGO rechazado: {}", e.getStatusCode());
      throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }
  }
}
