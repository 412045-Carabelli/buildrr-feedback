import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AplicacionSeleccionadaService } from '../../../../services/aplicaciones/aplicacion-seleccionada.service';
import { EstadoTicket, Producto, TicketResponse } from '../../../../core/models/models';
import { NOMBRE_ESTADO } from '../../../../core/constants/estado-labels';
import { NOMBRE_PRODUCTO } from '../../../../core/constants/producto-labels';
import { LayoutHeaderComponent } from '../../../../shared/layout-header/layout-header.component';

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
    ToastModule,
    LayoutHeaderComponent
  ],
  providers: [MessageService],
  templateUrl: './tickets-list.component.html'
})
export class TicketsListComponent implements OnInit {
  tickets: TicketResponse[] = [];
  cargando = false;
  opcionesEstado = OPCIONES_ESTADO;
  /** Puede venir del dashboard con varios (ej. pendientes = 3 estados) — el dropdown manual solo permite uno. */
  filtroEstados: EstadoTicket[] | null = null;

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
      .pipe(switchMap((producto) => this.ticketsService.listar(producto, this.filtroEstados)))
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

  get filtroDropdown(): EstadoTicket | null {
    return this.filtroEstados?.length === 1 ? this.filtroEstados[0] : null;
  }

  filtrarPorEstado(estado: EstadoTicket | null): void {
    this.filtroEstados = estado ? [estado] : null;
    this.router.navigate([], { relativeTo: this.route, queryParams: estado ? { estado } : {} });

    this.cargando = true;
    this.ticketsService.listar(this.aplicacionSeleccionadaService.seleccionadaActual, this.filtroEstados).subscribe({
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
}
