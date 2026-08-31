import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, OrigenCuenta } from '../../core/models/models';

const TOKEN_KEY = 'buildrr_feedback_token';
const ORIGEN_KEY = 'buildrr_feedback_origen';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((respuesta) => {
        localStorage.setItem(TOKEN_KEY, respuesta.token);
        localStorage.setItem(ORIGEN_KEY, respuesta.origen);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ORIGEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getOrigen(): OrigenCuenta | null {
    return localStorage.getItem(ORIGEN_KEY) as OrigenCuenta | null;
  }

  estaLogueado(): boolean {
    return this.getToken() !== null;
  }
}
