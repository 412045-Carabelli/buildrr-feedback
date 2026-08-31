package ar.buildrr.feedback.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Valida el JWT de la request contra 2 secrets fijos conocidos: el de
 * auth-service de SGO (tokens que este backend nunca firma) y el propio de
 * buildrr-feedback (tokens de cuentas FrezCo, ver auth/mediator). No hay
 * selección arbitraria por el cliente — se prueba primero uno, después el
 * otro. Ver docs/00-arquitectura.md.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProperties jwtProperties;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    Claims claims = parsear(token, jwtProperties.getSecret());
    OrigenCuenta origen = OrigenCuenta.SGO;

    if (claims == null) {
      claims = parsear(token, jwtProperties.getOwnSecret());
      origen = OrigenCuenta.FRESCO;
    }

    if (claims != null) {
      Object userIdClaim = claims.get("userId");
      Long userId = userIdClaim != null ? ((Number) userIdClaim).longValue() : null;
      String username = (String) claims.get("username");
      String rol = (String) claims.getOrDefault("rol", "USER");
      Object orgIdObj = claims.get("organizacionId");
      String organizacionId = orgIdObj != null ? String.valueOf(orgIdObj) : null;

      AuthenticatedUser user = new AuthenticatedUser(userId, username, rol, organizacionId, origen);
      var authToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
  }

  private Claims parsear(String token, String secret) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (SignatureException e) {
      return null; // no era este secret, se prueba el otro
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token JWT inválido: {}", e.getMessage());
      return null;
    }
  }
}
