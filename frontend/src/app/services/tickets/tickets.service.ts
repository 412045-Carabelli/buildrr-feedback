import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CambiarEstadoRequest,
  EditarTicketRequest,
  EstadisticasTicketResponse,
  EstadoTicket,
  HistorialEstadoResponse,
  Producto,
  TicketRequest,
  TicketResponse
} from '../../core/models/models';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private apiUrl = `${environment.apiUrl}/api/tickets`;

  constructor(private http: HttpClient) {}

  /**
   * @param producto opcional — la app seleccionada en el navbar. Sin filtro, todas las accesibles.
   * @param estados opcional — uno o más, para los accesos rápidos del dashboard (ej. pendientes = 3 estados).
   */
  listar(producto?: Producto | null, estados?: EstadoTicket[] | null): Observable<TicketResponse[]> {
    let params = new HttpParams();
    if (producto) params = params.set('producto', producto);
    if (estados?.length) params = params.set('estado', estados.join(','));
    return this.http.get<TicketResponse[]>(this.apiUrl, { params });
  }

  estadisticas(producto?: Producto | null): Observable<EstadisticasTicketResponse> {
    const params = producto ? new HttpParams().set('producto', producto) : undefined;
    return this.http.get<EstadisticasTicketResponse>(`${this.apiUrl}/stats`, { params });
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

  editar(id: number, payload: EditarTicketRequest): Observable<TicketResponse> {
    return this.http.put<TicketResponse>(`${this.apiUrl}/${id}`, payload);
  }

  historial(id: number): Observable<HistorialEstadoResponse[]> {
    return this.http.get<HistorialEstadoResponse[]>(`${this.apiUrl}/${id}/historial`);
  }
}
