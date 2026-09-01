package ar.buildrr.feedback.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Confía en los headers de identidad que inyecta el API Gateway compartido de
 * Buildr (auth-service detrás) después de validar el JWT — mismo patrón que
 * frezco/backend/.../GatewayAuthFilter.java. Este servicio nunca ve el JWT ni
 * la contraseña: si los headers no están, la request no pasó por el gateway.
 *
 * A diferencia de FrezCo (tenant único, organización fija), acá no se valida
 * una organización puntual: Pablo (SGO) y la dueña de FrezCo son
 * organizaciones distintas, ambas legítimas.
 */
@Component
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String userId = request.getHeader("X-User-Id");
    String username = request.getHeader("X-Username");
    String rol = request.getHeader("X-User-Rol");
    String organizacionId = request.getHeader("X-Organizacion-Id");

    if (userId == null || username == null) {
      // No seteamos autenticación: SecurityConfig decide si la ruta la
      // necesita. No es un 401 automático (el health check, por ejemplo,
      // no manda estos headers y tampoco los necesita).
      filterChain.doFilter(request, response);
      return;
    }

    try {
      AuthenticatedUser usuario = new AuthenticatedUser(Long.valueOf(userId), username, rol, organizacionId);
      var authToken = new UsernamePasswordAuthenticationToken(usuario, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authToken);
    } catch (NumberFormatException e) {
      responder(response, HttpServletResponse.SC_UNAUTHORIZED, "Identidad inválida del gateway");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void responder(HttpServletResponse response, int status, String mensaje) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, null);
    objectMapper.writeValue(response.getWriter(), Map.of("message", mensaje));
  }
}
