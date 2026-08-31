import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdjuntoResponse } from '../../core/models/models';

@Injectable({ providedIn: 'root' })
export class AdjuntosService {
  private apiUrl = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  subir(ticketId: number, archivo: File, historialEstadoId?: number): Observable<AdjuntoResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    if (historialEstadoId != null) {
      formData.append('historialEstadoId', String(historialEstadoId));
    }
    return this.http.post<AdjuntoResponse>(`${this.apiUrl}/tickets/${ticketId}/adjuntos`, formData);
  }

  listarPorTicket(ticketId: number): Observable<AdjuntoResponse[]> {
    return this.http.get<AdjuntoResponse[]>(`${this.apiUrl}/tickets/${ticketId}/adjuntos`);
  }

  descargar(adjuntoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/adjuntos/${adjuntoId}/descargar`, { responseType: 'blob' });
  }
}
