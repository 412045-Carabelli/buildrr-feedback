import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../services/tickets/tickets.service';
import { AplicacionSeleccionadaService } from '../../services/aplicaciones/aplicacion-seleccionada.service';
import { EstadisticasTicketResponse, EstadoTicket } from '../../core/models/models';
import { LayoutHeaderComponent } from '../../shared/layout-header/layout-header.component';
import { KpiCardComponent } from '../../shared/kpi-card/kpi-card.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ToastModule, LayoutHeaderComponent, KpiCardComponent],
  providers: [MessageService],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  stats: EstadisticasTicketResponse | null = null;
  cargando = false;

  constructor(
    private ticketsService: TicketsService,
    private aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // Igual que la lista de tickets: se re-suscribe cuando cambia la app
    // seleccionada en el navbar para no quedar mostrando números de otro producto.
    this.cargando = true;
    this.aplicacionSeleccionadaService.seleccionada$
      .pipe(switchMap((producto) => this.ticketsService.estadisticas(producto)))
      .subscribe({
        next: (data) => {
          this.stats = data;
          this.cargando = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las métricas' });
          this.cargando = false;
        }
      });
  }

  irATickets(estados?: EstadoTicket[]): void {
    this.router.navigate(['/tickets'], { queryParams: estados?.length ? { estado: estados.join(',') } : {} });
  }

  irAUsuarios(): void {
    this.router.navigate(['/admin/usuarios']);
  }
}
