import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AplicacionSeleccionadaService } from '../../../../services/aplicaciones/aplicacion-seleccionada.service';
import { EstadoTicket, Producto, TicketResponse } from '../../../../core/models/models';
import { NOMBRE_ESTADO } from '../../../../core/constants/estado-labels';
import { NOMBRE_PRODUCTO } from '../../../../core/constants/producto-labels';
import { LayoutHeaderComponent } from '../../../../shared/layout-header/layout-header.component';

/** Sin filtro de estado explícito y con "mostrar completados" apagado, se ocultan estos. */
const ESTADOS_SIN_COMPLETADO: EstadoTicket[] = ['NUEVO', 'EN_PROGRESO', 'TESTING', 'ANULADO'];

const OPCIONES_ESTADO: { label: string; value: EstadoTicket | null }[] = [
  { label: 'Todos los estados', value: null },
  { label: 'Nuevo', value: 'NUEVO' },
  { label: 'En progreso', value: 'EN_PROGRESO' },
  { label: 'Testing', value: 'TESTING' },
  { label: 'Completado', value: 'COMPLETADO' },
  { label: 'Anulado', value: 'ANULADO' }
];

@Component({
  selector: 'app-tickets-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    TableModule,
    TagModule,
    ButtonModule,
    SelectModule,
    CheckboxModule,
    InputTextModule,
    ToastModule,
    LayoutHeaderComponent
  ],
  providers: [MessageService],
  templateUrl: './tickets-list.component.html'
})
export class TicketsListComponent implements OnInit, OnDestroy {
  tickets: TicketResponse[] = [];
  cargando = false;
  opcionesEstado = OPCIONES_ESTADO;
  /** Puede venir del dashboard con varios (ej. pendientes = 3 estados) — el dropdown manual solo permite uno. */
  filtroEstados: EstadoTicket[] | null = null;
  /** Con el filtro de estado en "Todos", oculta COMPLETADO salvo que se tilde esto. */
  mostrarCompletados = false;
  busqueda = '';

  private busqueda$ = new Subject<void>();

  constructor(
    private ticketsService: TicketsService,
    private aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    const estadoQuery = this.route.snapshot.queryParamMap.get('estado');
    this.filtroEstados = estadoQuery ? (estadoQuery.split(',') as EstadoTicket[]) : null;

    // Se re-suscribe cada vez que cambia la app seleccionada en el navbar —
    // así la lista refleja el filtro sin depender de que la navegación
    // recree el componente.
    this.cargando = true;
    this.aplicacionSeleccionadaService.seleccionada$
      .pipe(switchMap((producto) => this.ticketsService.listar(producto, this.estadosEfectivos(), this.busqueda)))
      .subscribe({
        next: (data) => {
          this.tickets = data;
          this.cargando = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los tickets' });
          this.cargando = false;
        }
      });

    // Debounce del buscador de texto — no dispara un request por cada tecla.
    this.busqueda$.pipe(debounceTime(300)).subscribe(() => this.cargarTickets());
  }

  ngOnDestroy(): void {
    this.busqueda$.complete();
  }

  get filtroDropdown(): EstadoTicket | null {
    return this.filtroEstados?.length === 1 ? this.filtroEstados[0] : null;
  }

  /** Sin filtro de estado explícito, aplica el default de ocultar COMPLETADO. */
  private estadosEfectivos(): EstadoTicket[] | null {
    if (this.filtroEstados) return this.filtroEstados;
    return this.mostrarCompletados ? null : ESTADOS_SIN_COMPLETADO;
  }

  filtrarPorEstado(estado: EstadoTicket | null): void {
    this.filtroEstados = estado ? [estado] : null;
    this.router.navigate([], { relativeTo: this.route, queryParams: estado ? { estado } : {} });
    this.cargarTickets();
  }

  onToggleMostrarCompletados(): void {
    this.cargarTickets();
  }

  onBuscar(): void {
    this.busqueda$.next();
  }

  private cargarTickets(): void {
    this.cargando = true;
    this.ticketsService
      .listar(this.aplicacionSeleccionadaService.seleccionadaActual, this.estadosEfectivos(), this.busqueda)
      .subscribe({
        next: (data) => {
          this.tickets = data;
          this.cargando = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los tickets' });
          this.cargando = false;
        }
      });
  }

  verTicket(id: number): void {
    this.router.navigate(['/tickets', id]);
  }

  severityEstado(estado: EstadoTicket): 'info' | 'warn' | 'secondary' | 'success' | 'danger' {
    switch (estado) {
      case 'NUEVO': return 'info';
      case 'EN_PROGRESO': return 'warn';
      case 'TESTING': return 'secondary';
      case 'COMPLETADO': return 'success';
      case 'ANULADO': return 'danger';
    }
  }

  nombreEstado(estado: EstadoTicket): string {
    return NOMBRE_ESTADO[estado];
  }

  nombreProducto(producto: Producto): string {
    return NOMBRE_PRODUCTO[producto];
  }

  /**
   * Exporta el listado actual (con el filtro aplicado) a un .md con el
   * detalle de cada ticket — pensado para pegarlo/adjuntarlo tal cual en
   * una conversación con Claude Code. Sin librería: Blob + <a download>,
   * mismo criterio que "no PDF lib" del CLAUDE.md.
   */
  exportar(): void {
    const fecha = new Date().toISOString().slice(0, 10);
    const lineas = [
      `# Tickets Buildrr Feedback — ${fecha}`,
      `Total: ${this.tickets.length}`,
      ''
    ];

    for (const t of this.tickets) {
      lineas.push(`## #${t.id} — ${t.titulo}`);
      lineas.push(`- Tipo: ${t.tipo === 'BUG' ? 'Bug' : 'Función nueva'}`);
      lineas.push(`- Producto: ${this.nombreProducto(t.producto)}`);
      lineas.push(`- Módulo: ${t.modulo || '—'}`);
      lineas.push(`- Estado: ${this.nombreEstado(t.estado)}`);
      lineas.push(`- Fecha: ${t.fecha}`);
      lineas.push(`- Creado por: ${t.creadoPor} (${t.creadoEn})`);
      if (t.ultimaActualizacion) lineas.push(`- Última actualización: ${t.ultimaActualizacion}`);
      lineas.push('');
      lineas.push(t.descripcion?.trim() || '_Sin descripción._');
      lineas.push('');
    }

    const blob = new Blob([lineas.join('\n')], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `tickets-buildrr-feedback-${fecha}.md`;
    link.click();
    URL.revokeObjectURL(url);
  }
}
