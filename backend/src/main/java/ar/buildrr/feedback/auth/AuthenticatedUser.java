package ar.buildrr.feedback.auth;

/**
 * Identidad extraída del JWT de auth-service (SGO). No hay tabla de usuarios
 * propia acá — esto es todo lo que este servicio sabe de quién hace la request.
 */
public record AuthenticatedUser(Long userId, String username, String rol, String organizacionId) {
}
