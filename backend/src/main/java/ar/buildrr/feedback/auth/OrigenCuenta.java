package ar.buildrr.feedback.auth;

/** De qué sistema viene la identidad: SGO (JWT real de auth-service) o FRESCO (login propio, ver mediator/). */
public enum OrigenCuenta {
  SGO,
  FRESCO
}
