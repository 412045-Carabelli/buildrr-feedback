package ar.buildrr.feedback.auth.mediator;

import ar.buildrr.feedback.auth.FrezcoProperties;
import ar.buildrr.feedback.auth.JwtProperties;
import ar.buildrr.feedback.auth.OrigenCuenta;
import ar.buildrr.feedback.auth.dto.LoginRequest;
import ar.buildrr.feedback.auth.dto.LoginResponse;
import ar.buildrr.feedback.auth.exception.CredencialesInvalidasException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * Único usuario de FrezCo (la dueña del emprendimiento). `soporta()` es un
 * chequeo local barato (usuario conocido, `frezco.app-user`) solo para
 * decidir el ruteo — la contraseña NUNCA se duplica acá: se valida llamando
 * al login real de FrezCo (`POST {frezco.login-url}/api/auth/login`, mismo
 * contrato que ar.frezco.config.AutenticacionController: campos `usuario` y
 * `clave`, sesión por cookie). Si FrezCo confirma, buildrr-feedback firma su
 * PROPIO JWT (jwt.own-secret) — nunca imita el de SGO.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class FrezcoAuthColleague implements AuthColleague {

  private final FrezcoProperties frezcoProperties;
  private final JwtProperties jwtProperties;
  private final RestClient.Builder restClientBuilder;

  @Override
  public boolean soporta(LoginRequest request) {
    return frezcoProperties.getAppUser() != null
        && frezcoProperties.getAppUser().equalsIgnoreCase(request.getUsuario());
  }

  @Override
  public LoginResponse autenticar(LoginRequest request) {
    validarContraFrezco(request);

    String token = Jwts.builder()
        .subject(request.getUsuario())
        .claim("username", request.getUsuario())
        .claim("rol", "CLIENTE")
        .claim("origen", OrigenCuenta.FRESCO.name())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plus(8, ChronoUnit.HOURS)))
        .signWith(Keys.hmacShaKeyFor(jwtProperties.getOwnSecret().getBytes(StandardCharsets.UTF_8)))
        .compact();

    return LoginResponse.builder().token(token).origen(OrigenCuenta.FRESCO).build();
  }

  private void validarContraFrezco(LoginRequest request) {
    try {
      restClientBuilder.build()
          .post()
          .uri(frezcoProperties.getLoginUrl() + "/api/auth/login")
          .body(Map.of("usuario", request.getUsuario(), "clave", request.getPassword()))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException e) {
      log.warn("Login FrezCo rechazado: {}", e.getStatusCode());
      throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }
  }
}
