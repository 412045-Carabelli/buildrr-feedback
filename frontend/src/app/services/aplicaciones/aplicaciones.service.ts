import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AplicacionAccesoResponse } from '../../core/models/models';

@Injectable({ providedIn: 'root' })
export class AplicacionesService {
  private apiUrl = `${environment.apiUrl}/api/usuario-aplicacion`;

  constructor(private http: HttpClient) {}

  misAplicaciones(): Observable<AplicacionAccesoResponse[]> {
    return this.http.get<AplicacionAccesoResponse[]>(`${this.apiUrl}/mis-aplicaciones`);
  }
}
