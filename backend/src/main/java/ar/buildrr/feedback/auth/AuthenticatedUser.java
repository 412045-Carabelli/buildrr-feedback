package ar.buildrr.feedback.auth;

/**
 * Identidad que ya validó el API Gateway compartido de Buildr (auth-service
 * detrás) — este servicio no ve JWT ni contraseña, solo los headers que
 * inyecta el gateway. Ver GatewayAuthFilter.
 */
public record AuthenticatedUser(Long userId, String username, String rol, String organizacionId) {
}
