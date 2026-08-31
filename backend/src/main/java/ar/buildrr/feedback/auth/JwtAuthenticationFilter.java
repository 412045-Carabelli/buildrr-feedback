package ar.buildrr.feedback.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Valida el JWT de la request contra el secret de auth-service de SGO —
 * única fuente de identidad del ecosistema (ver docs/00-arquitectura.md). No
 * hay llamada de red en cada request, solo verificación de firma/expiración.
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
    try {
      Claims claims = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseSignedClaims(token)
          .getPayload();

      Long userId = ((Number) claims.get("userId")).longValue();
      String username = (String) claims.get("username");
      String rol = (String) claims.getOrDefault("rol", "USER");
      Object orgIdObj = claims.get("organizacionId");
      String organizacionId = orgIdObj != null ? String.valueOf(orgIdObj) : null;

      AuthenticatedUser user = new AuthenticatedUser(userId, username, rol, organizacionId);
      var authToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authToken);

    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token JWT inválido: {}", e.getMessage());
      // no seteamos autenticación; SecurityConfig decide si la ruta requiere auth
    }

    filterChain.doFilter(request, response);
  }
}
