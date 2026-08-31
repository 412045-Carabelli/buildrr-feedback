package ar.buildrr.feedback.auth.mediator;

import ar.buildrr.feedback.auth.OrigenCuenta;
import ar.buildrr.feedback.auth.SgoProperties;
import ar.buildrr.feedback.auth.dto.LoginRequest;
import ar.buildrr.feedback.auth.dto.LoginResponse;
import ar.buildrr.feedback.auth.exception.CredencialesInvalidasException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Reenvía el login al auth-service real de SGO (vía api-gateway) y re-emite
 * el JWT que devuelve tal cual — este backend NUNCA firma tokens "de SGO", solo
 * los valida (ver JwtAuthenticationFilter). Es el fallback: si FrezcoAuthColleague
 * no reconoce el usuario, se prueba acá.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SgoAuthColleague implements AuthColleague {

  private final RestClient.Builder restClientBuilder;
  private final SgoProperties sgoProperties;

  @Override
  public boolean soporta(LoginRequest request) {
    return true; // fallback: todo lo que no sea la cuenta de FrezCo se intenta contra SGO
  }

  @Override
  @SuppressWarnings("unchecked")
  public LoginResponse autenticar(LoginRequest request) {
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
      return LoginResponse.builder().token(token).origen(OrigenCuenta.SGO).build();

    } catch (RestClientResponseException e) {
      log.warn("Login SGO rechazado: {}", e.getStatusCode());
      throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }
  }
}
