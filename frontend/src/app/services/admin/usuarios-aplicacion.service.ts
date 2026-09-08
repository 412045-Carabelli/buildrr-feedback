import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AltaUsuarioAplicacionRequest, Producto, UsuarioAplicacionResponse } from '../../core/models/models';

/** Panel de owner/admin: alta y baja de accesos por producto. Ver backend usuarioaplicacion/factory/. */
@Injectable({ providedIn: 'root' })
export class UsuariosAplicacionService {
  private apiUrl = `${environment.apiUrl}/api/admin/usuarios-aplicacion`;

  constructor(private http: HttpClient) {}

  listar(producto: Producto): Observable<UsuarioAplicacionResponse[]> {
    const params = new HttpParams().set('producto', producto);
    return this.http.get<UsuarioAplicacionResponse[]>(this.apiUrl, { params });
  }

  darDeAlta(payload: AltaUsuarioAplicacionRequest): Observable<UsuarioAplicacionResponse> {
    return this.http.post<UsuarioAplicacionResponse>(this.apiUrl, payload);
  }

  revocar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
