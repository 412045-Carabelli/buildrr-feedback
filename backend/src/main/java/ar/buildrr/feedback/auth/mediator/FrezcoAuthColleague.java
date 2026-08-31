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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Único usuario de FrezCo (la dueña del emprendimiento), mismas credenciales
 * que ya usa el backend de FrezCo (env vars, sin tabla de usuarios). Si
 * matchean, buildrr-feedback firma su PROPIO JWT (jwt.own-secret) — nunca
 * usa/imita el secret de SGO.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class FrezcoAuthColleague implements AuthColleague {

  private final FrezcoProperties frezcoProperties;
  private final JwtProperties jwtProperties;

  @Override
  public boolean soporta(LoginRequest request) {
    return frezcoProperties.getAppUser() != null
        && frezcoProperties.getAppUser().equalsIgnoreCase(request.getUsuario());
  }

  @Override
  public LoginResponse autenticar(LoginRequest request) {
    if (!frezcoProperties.getAppPassword().equals(request.getPassword())) {
      throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
    }

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
}
