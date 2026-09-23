import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, ForgotPasswordRequest, LoginRequest, LoginResponse, ResetPasswordRequest } from '../../core/models/models';

const TOKEN_KEY = 'buildrr_feedback_token';
const REFRESH_KEY = 'buildrr_feedback_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((respuesta) => this.guardarTokens(respuesta))
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
      tap((respuesta) => this.guardarTokens(respuesta))
    );
  }

  /** Mismo contrato que auth-service — envía el código de reset al mail. */
  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/forgot-password`, request);
  }

  /** Mismo contrato que auth-service — valida el código y cambia la contraseña. */
  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset-password`, request);
  }

  /**
   * Renueva la sesión con el refresh token de auth-service (30 días, y cada
   * refresh devuelve uno nuevo con 30 días más). Así la sesión dura un mes
   * desde el último uso aunque el access token venza a los 15 minutos.
   */
  refresh(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, { refreshToken: this.getRefreshToken() }).pipe(
      tap((respuesta) => this.guardarTokens(respuesta))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  }

  private guardarTokens(respuesta: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, respuesta.access_token);
    if (respuesta.refresh_token) {
      localStorage.setItem(REFRESH_KEY, respuesta.refresh_token);
    }
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
