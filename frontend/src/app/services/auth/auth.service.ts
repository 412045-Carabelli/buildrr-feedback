import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, LoginRequest, LoginResponse, ResetPasswordSinLoginRequest } from '../../core/models/models';

const TOKEN_KEY = 'buildrr_feedback_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((respuesta) => localStorage.setItem(TOKEN_KEY, respuesta.access_token))
    );
  }

  /**
   * Mismo endpoint que auth-service (ver com.auth.controller.AuthController)
   * usa en el resto del ecosistema — no reimplementa nada acá, este backend
   * ni siquiera tiene tabla de usuarios. Devuelve tokens nuevos (rota la
   * sesión), como el login.
   */
  changePassword(request: ChangePasswordRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/change-password`, request).pipe(
      tap((respuesta) => localStorage.setItem(TOKEN_KEY, respuesta.access_token))
    );
  }

  /** TEMPORAL — sin login, ver ResetPasswordSinLoginRequest. Sacar cuando vuelva a exigirse sesión. */
  resetPasswordSinLogin(request: ResetPasswordSinLoginRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset-password-temporal`, request);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  estaLogueado(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Username del claim del JWT (mismo que manda el gateway como
   * X-Username) — solo para decidir qué mostrar en la UI (ej. botón
   * "Editar" si sos el creador). El backend vuelve a validar todo con su
   * propio X-Username, esto no es una fuente de autorización.
   */
  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.username ?? null;
    } catch {
      return null;
    }
  }
}
