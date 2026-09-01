import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CambiarEstadoRequest, Producto, TicketRequest, TicketResponse } from '../../core/models/models';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private apiUrl = `${environment.apiUrl}/api/tickets`;

  constructor(private http: HttpClient) {}

  /** @param producto opcional — la app seleccionada en el navbar. Sin filtro, todas las accesibles. */
  listar(producto?: Producto | null): Observable<TicketResponse[]> {
    const params = producto ? new HttpParams().set('producto', producto) : undefined;
    return this.http.get<TicketResponse[]>(this.apiUrl, { params });
  }

  obtenerPorId(id: number): Observable<TicketResponse> {
    return this.http.get<TicketResponse>(`${this.apiUrl}/${id}`);
  }

  crear(payload: TicketRequest): Observable<TicketResponse> {
    return this.http.post<TicketResponse>(this.apiUrl, payload);
  }

  cambiarEstado(id: number, payload: CambiarEstadoRequest): Observable<TicketResponse> {
    return this.http.patch<TicketResponse>(`${this.apiUrl}/${id}/estado`, payload);
  }
}
