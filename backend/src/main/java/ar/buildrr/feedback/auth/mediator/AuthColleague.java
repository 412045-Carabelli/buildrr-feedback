package ar.buildrr.feedback.auth.mediator;

import ar.buildrr.feedback.auth.dto.LoginRequest;
import ar.buildrr.feedback.auth.dto.LoginResponse;

/**
 * Patrón Mediator: cada colleague sabe validar credenciales contra UNA fuente
 * de identidad (SGO o FrezCo) y no conoce a los demás colleagues. LoginMediator
 * es el único que los coordina. Ver docs/00-arquitectura.md.
 */
public interface AuthColleague {

  boolean soporta(LoginRequest request);

  LoginResponse autenticar(LoginRequest request);
}
