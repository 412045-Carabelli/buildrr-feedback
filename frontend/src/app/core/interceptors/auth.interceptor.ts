import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, map, shareReplay, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

/**
 * Ante un 401 intenta renovar la sesión con el refresh token (auth-service,
 * POST /auth/refresh, 30 días) y reintenta el request una sola vez. Si no hay
 * refresh token o el refresh falla, limpia la sesión y manda a /login.
 * Varios 401 simultáneos comparten el mismo refresh en vuelo.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private refreshEnCurso: Observable<string> | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
      return next.handle(req);
    }

    return next.handle(this.conToken(req, this.authService.getToken())).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status !== 401 || this.esErrorDeNegocioDeAuth(req, error)) {
          return throwError(() => error);
        }
        if (!this.authService.getRefreshToken()) {
          this.cerrarSesion();
          return throwError(() => error);
        }
        return this.renovarToken().pipe(
          switchMap((token) => next.handle(this.conToken(req, token))),
          catchError((errorRefresh) => {
            this.cerrarSesion();
            return throwError(() => errorRefresh);
          })
        );
      })
    );
  }

  private renovarToken(): Observable<string> {
    if (!this.refreshEnCurso) {
      this.refreshEnCurso = this.authService.refresh().pipe(
        map((respuesta) => respuesta.access_token),
        finalize(() => (this.refreshEnCurso = null)),
        shareReplay(1)
      );
    }
    return this.refreshEnCurso;
  }

  /**
   * El gateway responde 401 sin body cuando el JWT falta o venció. auth-service
   * también responde 401 con body `{ message }` para errores propios (ej.
   * contraseña actual incorrecta en /auth/change-password): eso no es sesión
   * vencida, no se renueva ni se desloguea — el componente muestra el error.
   */
  private esErrorDeNegocioDeAuth(req: HttpRequest<any>, error: HttpErrorResponse): boolean {
    return req.url.includes('/auth/') && !!error.error?.message;
  }

  private conToken(req: HttpRequest<any>, token: string | null): HttpRequest<any> {
    return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  }

  private cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
