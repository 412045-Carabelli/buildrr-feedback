import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AplicacionAccesoResponse, Producto } from '../../core/models/models';
import { AplicacionesService } from './aplicaciones.service';

const STORAGE_KEY = 'buildrr_feedback_aplicacion_seleccionada';

/**
 * Barra de persistencia del navbar: qué aplicación (producto) está eligiendo
 * el usuario ahora mismo. Determina a qué producto se cargan los bugs nuevos
 * y qué tickets se ven en el dashboard. Se persiste en localStorage — dura
 * entre navegaciones y recargas, no entre dispositivos.
 */
@Injectable({ providedIn: 'root' })
export class AplicacionSeleccionadaService {
  private aplicacionesSubject = new BehaviorSubject<AplicacionAccesoResponse[]>([]);
  aplicaciones$ = this.aplicacionesSubject.asObservable();

  private seleccionadaSubject = new BehaviorSubject<Producto | null>(null);
  seleccionada$ = this.seleccionadaSubject.asObservable();

  constructor(private aplicacionesService: AplicacionesService) {}

  cargar(): Observable<AplicacionAccesoResponse[]> {
    return this.aplicacionesService.misAplicaciones().pipe(
      tap((aplicaciones) => {
        this.aplicacionesSubject.next(aplicaciones);
        this.seleccionadaSubject.next(this.resolverSeleccionInicial(aplicaciones));
      })
    );
  }

  seleccionar(producto: Producto): void {
    localStorage.setItem(STORAGE_KEY, producto);
    this.seleccionadaSubject.next(producto);
  }

  get seleccionadaActual(): Producto | null {
    return this.seleccionadaSubject.value;
  }

  get aplicacionesActuales(): AplicacionAccesoResponse[] {
    return this.aplicacionesSubject.value;
  }

  esAdminDe(producto: Producto): boolean {
    return this.aplicacionesActuales.some((a) => a.producto === producto && a.rol === 'ADMIN');
  }

  private resolverSeleccionInicial(aplicaciones: AplicacionAccesoResponse[]): Producto | null {
    if (aplicaciones.length === 0) return null;

    const guardada = localStorage.getItem(STORAGE_KEY) as Producto | null;
    if (guardada && aplicaciones.some((a) => a.producto === guardada)) {
      return guardada;
    }
    return aplicaciones[0].producto;
  }
}
